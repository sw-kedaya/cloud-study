package cn.itcast.mq.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAMQPTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSimpleQueue() {
        String queueName = "simple.queue";
        String message = "hello amqp";
        rabbitTemplate.convertAndSend(queueName, message);
    }

    @Test
    public void testWokeQueue() throws InterruptedException {
        String queueName = "simple.queue";
        String message = "hello amqp---";
        for (int i = 0; i < 50; i++) {
            rabbitTemplate.convertAndSend(queueName, message + i);
            Thread.sleep(20);
        }
    }

    @Test
    public void testFanoutExchangeQueue() {
        String exchange = "fanoutExchange";
        String message = "hello every one~";
        rabbitTemplate.convertAndSend(exchange, "", message);
    }

    @Test
    public void testDirectExchangeQueue() {
        String exchange = "direct.exchange";
        String key = "red";
        String message = "hello " + key;
        rabbitTemplate.convertAndSend(exchange, key, message);
    }

    @Test
    public void testTopicExchangeQueue() {
        String exchange = "topic.exchange";
        String key = "nintendo.news";
        String message = "任天堂的新闻";
        rabbitTemplate.convertAndSend(exchange, key, message);
    }

    @Test
    public void testObjectQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "yy");
        map.put("age", 22);
        rabbitTemplate.convertAndSend("object.queue", map);
    }
}
