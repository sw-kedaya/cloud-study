package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class HotelDocumentTest {
    private RestHighLevelClient client;

    @Test
    void testInit() {
        System.out.println(client);
    }

    @Autowired
    private IHotelService hotelService;

    @Test
    void testDocumentCreate() throws IOException {
        //查出酒店数据
        Hotel hotel = hotelService.getById(36934);
        //把hotel转成文档类型
        HotelDoc hotelDoc = new HotelDoc(hotel);
        //准备request请求
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
        //准备json数据
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        //发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDocumentGet() throws IOException {

        GetRequest request = new GetRequest("hotel", "36934");

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        String json = response.getSourceAsString();

        System.out.println(json);
    }

    @Test
    void testDocumentUpdate() throws IOException {
        UpdateRequest request = new UpdateRequest("hotel", "36934");

        request.doc(
                "starName", "四钻",
                "price", 436
        );

        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);

        System.out.println(update);
    }

    @Test
    void testDocumentDelete() throws IOException {
        DeleteRequest request = new DeleteRequest("hotel", "36934");

        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);

        System.out.println(delete);
    }

    @Test
    void testBulk() throws IOException {
        // 4.从数据库中查询所有数据
        List<Hotel> hotels = hotelService.list();
        // 2.创建请求
        BulkRequest request = new BulkRequest();
        // 5.把所有查询的数据封装进请求
        for (Hotel hotel : hotels) {
            // 6.把数据转换成文档格式
            HotelDoc hotelDoc = new HotelDoc(hotel);
            // 3.添加请求数据
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        }
        // 1.发送请求
        client.bulk(request, RequestOptions.DEFAULT);
    }

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                //可以弄多个参数，逗号分隔
                HttpHost.create("http://192.168.233.100:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }
}
