package cn.itcast.order.service;

import cn.itcast.order.client.UserClient;
import cn.itcast.order.mapper.OrderMapper;
import cn.itcast.order.pojo.Order;
import cn.itcast.order.pojo.User;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserClient userClient;

    public Order queryOrderById(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);

//        String url = "http://userservice/user/" + order.getUserId();
//        User user = restTemplate.getForObject(url, User.class);
        User user = userClient.findById(order.getUserId());
        order.setUser(user);

        // 4.返回
        return order;
    }

    @SentinelResource("goods")
    public void queryGoods(){
        System.err.println("查询商品");
    }
}
