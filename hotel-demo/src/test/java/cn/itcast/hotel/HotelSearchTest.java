package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HotelSearchTest {
    private RestHighLevelClient client;

    @Test
    void testSearchMatchAll() throws IOException {
        // 准备请求
        SearchRequest request = new SearchRequest("hotel");
        // 设置查询内容
        request.source().query(QueryBuilders.matchAllQuery());
        // 发送search请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 解析响应数据
        ResponseHandle(response);
    }

    @Test
    void testSearchMatch() throws IOException {
        // 准备请求
        SearchRequest request = new SearchRequest("hotel");
        // 设置查询内容
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        // 发送search请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 解析响应数据
        ResponseHandle(response);
    }

    @Test
    void testSearchBoolTermRange() throws IOException {
        SearchRequest request = new SearchRequest("hotel");

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("city", "上海"));
        boolQuery.filter(QueryBuilders.rangeQuery("price").lte(250));
        // 就像mp放条件一样
        request.source().query(boolQuery);

        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        ResponseHandle(search);
    }

    @Test
    void testSearchOrderPage() throws IOException {
        // 前端传来的页面数据
        int page = 2, size = 5;

        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().sort("price", SortOrder.ASC);
        request.source().from((page - 1) * size).size(size);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        ResponseHandle(response);
    }

    @Test
    void testSearchHighLight() throws IOException {
        SearchRequest request = new SearchRequest("hotel");

        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        request.source().highlighter(new HighlightBuilder()
                .field("name")
                .requireFieldMatch(false));

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        ResponseHandle(response);
    }

    private void ResponseHandle(SearchResponse response) {
        // 解析响应数据
        SearchHits searchHits = response.getHits();
        // 获取总共多少条数据
        TotalHits totalHits = searchHits.getTotalHits();
        System.out.println(totalHits);
        // 获取每条数据并转换成对象
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            // 建议根据es查出的内容层层取
            String json = hit.getSourceAsString();
            // 把json转成对象
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);

            // 处理高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            // 这里不用工具类可以用：
//            if(highlightFields != null || highlightFields.size() != 0)
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField field = highlightFields.get("name");
                if (field != null){
                    String name = field.getFragments()[0].string();
                    hotelDoc.setName(name);
                }
            }

            System.out.println(hotelDoc);
        }
    }

    @Test
    void testAggregations() throws IOException {

        SearchRequest request = new SearchRequest("hotel");

        request.source().size(0);
        request.source().aggregation(AggregationBuilders
                .terms("BucketName")
                .field("brand")
                .size(10));

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        //解析数据
        Aggregations aggregations = response.getAggregations();
        Terms bucketName = aggregations.get("BucketName");
        List<? extends Terms.Bucket> buckets = bucketName.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println(keyAsString);
        }
    }

    @Test
    void testSuggestion() throws IOException {
        SearchRequest request = new SearchRequest("hotel");

        request.source().suggest(new SuggestBuilder().addSuggestion(
                "mySuggestion",
                SuggestBuilders
                        .completionSuggestion("suggestion")
                        .prefix("t")
                        .skipDuplicates(true)
                        .size(10)));

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 解析数据
        Suggest suggest = response.getSuggest();
        CompletionSuggestion mySuggestion = suggest.getSuggestion("mySuggestion");
        List<CompletionSuggestion.Entry.Option> options = mySuggestion.getOptions();
        for (CompletionSuggestion.Entry.Option option : options) {
            System.out.println(option.getText());
        }
    }

    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.233.100:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }
}
