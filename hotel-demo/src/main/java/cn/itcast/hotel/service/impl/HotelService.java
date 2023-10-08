package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestPrams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult selectPage(RequestPrams prams) {
        try {
            SearchRequest request = new SearchRequest("hotel");

            // 把整个查询封装一下
            FunctionScoreQueryBuilder functionScoreQueryBuilder = BoolQueryBuild(prams);

            request.source().query(functionScoreQueryBuilder);

            int page = prams.getPage();
            int size = prams.getSize();
            request.source().from((page - 1) * size).size(size);

            String location = prams.getLocation();
            if (location != null && !location.equals("")) {
                request.source().sort(SortBuilders
                        .geoDistanceSort("location", new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS));
            }

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            return ResponseHandle(response);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public Map<String, List<String>> filters(RequestPrams requestPrams) {
        try {
            SearchRequest request = new SearchRequest("hotel");

            //添加查询条件
            request.source().query(BoolQueryBuild(requestPrams));

            request.source().size(0);
            //设置聚合的内容
            setAggregationParameter(request);

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            Aggregations aggregations = response.getAggregations();

            Map<String, List<String>> result = new HashMap<>();

            ArrayList<String> brandList = getAggBucketKey(aggregations, "brandAgg");
            result.put("brand", brandList);

            ArrayList<String> cityList = getAggBucketKey(aggregations, "cityAgg");
            result.put("city", cityList);

            ArrayList<String> starList = getAggBucketKey(aggregations, "starNameAgg");
            result.put("starName", starList);

            return result;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public List<String> suggestion(String prefix) {
        try {
            SearchRequest request = new SearchRequest("hotel");

            request.source().suggest(new SuggestBuilder().addSuggestion(
                    "mySuggestion",
                    SuggestBuilders
                            .completionSuggestion("suggestion")
                            .prefix(prefix)
                            .skipDuplicates(true)
                            .size(10)));

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            // 解析数据
            Suggest suggest = response.getSuggest();
            CompletionSuggestion mySuggestion = suggest.getSuggestion("mySuggestion");
            List<CompletionSuggestion.Entry.Option> options = mySuggestion.getOptions();

            List<String> list = new ArrayList<>(options.size());

            for (CompletionSuggestion.Entry.Option option : options) {
                String text = option.getText().string();
                list.add(text);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertHotelById(String id) {
        try {
            Hotel hotel = this.getById(id);
            HotelDoc hotelDoc = new HotelDoc(hotel);

            IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());

            request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);

            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteHotelById(String id) {
        try {
            DeleteRequest request = new DeleteRequest("hotel", id);

            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<String> getAggBucketKey(Aggregations aggregations, String name) {
        Terms agg = aggregations.get(name);
        List<? extends Terms.Bucket> brandBuckets = agg.getBuckets();
        ArrayList<String> list = new ArrayList<>();
        for (Terms.Bucket brandBucket : brandBuckets) {
            String brand = brandBucket.getKeyAsString();
            list.add(brand);
        }
        return list;
    }

    private void setAggregationParameter(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(100));
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg")
                .field("city")
                .size(100));
        request.source().aggregation(AggregationBuilders
                .terms("starNameAgg")
                .field("starName")
                .size(100));
    }

    private FunctionScoreQueryBuilder BoolQueryBuild(RequestPrams prams) {
        // bool多条件查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 搜索内容
        String key = prams.getKey();
        if (key == null || "".equals(key)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }
        // 城市
        if (prams.getCity() != null && !prams.getCity().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("city", prams.getCity()));
        }
        // 星级
        if (prams.getStarName() != null && !prams.getStarName().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("starName", prams.getStarName()));
        }
        // 品牌
        if (prams.getBrand() != null && !prams.getBrand().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("brand", prams.getBrand()));
        }
        // 价格
        if (prams.getMinPrice() != null && prams.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders
                    .rangeQuery("price")
                    .lte(prams.getMaxPrice())
                    .gte(prams.getMinPrice()));
        }

        // 算分控制
        FunctionScoreQueryBuilder functionScoreQueryBuilder =
                QueryBuilders.functionScoreQuery(
                        boolQuery,
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        QueryBuilders.termQuery("isAD", true),
                                        ScoreFunctionBuilders.weightFactorFunction(10)
                                )
                        });

        return functionScoreQueryBuilder;
    }

    private PageResult ResponseHandle(SearchResponse response) {
        // 解析响应数据
        SearchHits searchHits = response.getHits();
        // 获取总共多少条数据
        TotalHits totalHits = searchHits.getTotalHits();
        long total = totalHits.value;
        // 获取每条数据并转换成对象
        SearchHit[] hits = searchHits.getHits();
        List<HotelDoc> hotels = new ArrayList<>();
        for (SearchHit hit : hits) {
            // 建议根据es查出的内容层层取
            String json = hit.getSourceAsString();
            // 把json转成对象
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);

            // 取出距离酒店的公里数
            Object[] sortValues = hit.getSortValues();
            if (sortValues.length > 0) {
                Object distance = sortValues[0];
                hotelDoc.setDistance(distance);
            }

            hotels.add(hotelDoc);
        }
        return new PageResult(total, hotels);
    }
}
