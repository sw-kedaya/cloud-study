package cn.itcast.mq.listener;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class SimpleQueueListener {
//    @RabbitListener(queues = "simple.queue")
//    public void listenSimpleQueue(String msg) {
//        System.out.println("消费者接收到了数据：" + msg);
//    }

    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueue1(String msg) {
        System.out.println("消费者1接收到了数据：" + msg + LocalDateTime.now());
    }

    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueue2(String msg) {
        System.err.println("消费者2接收到了数据：" + msg + LocalDateTime.now());
    }

    @RabbitListener(queues = "fanoutQueue1")
    public void listenFanoutExchangeQueue1(String msg) {
        System.out.println("消费了由FanoutExchange交换机分配给fanoutQueue1队列的数据：" + msg);
    }

    @RabbitListener(queues = "fanoutQueue2")
    public void listenFanoutExchangeQueue2(String msg) {
        System.out.println("消费了由FanoutExchange交换机分配给fanoutQueue2队列的数据：" + msg);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("direct.queue1"),
            exchange = @Exchange(value = "direct.exchange", type = ExchangeTypes.DIRECT),
            key = {"red", "blue"}
    ))
    public void listenDirectExchangeQueue1(String msg) {
        System.out.println("消费了由DirectExchange交换机分配给direct.queue1队列的数据：" + msg);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("direct.queue2"),
            exchange = @Exchange(value = "direct.exchange", type = ExchangeTypes.DIRECT),
            key = {"red", "yellow"}
    ))
    public void listenDirectExchangeQueue2(String msg) {
        System.out.println("消费了由DirectExchange交换机分配给direct.queue2队列的数据：" + msg);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("topic.queue1"),
            exchange = @Exchange(value = "topic.exchange", type = ExchangeTypes.TOPIC),
            key = "china.#"
    ))
    public void listenTopicExchangeQueue1(String msg) {
        System.out.println("消费了由TopicExchange交换机分配给topic.queue1队列的数据：" + msg);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("topic.queue2"),
            exchange = @Exchange(value = "topic.exchange", type = ExchangeTypes.TOPIC),
            key = "#.news"
    ))
    public void listenTopicExchangeQueue2(String msg) {
        System.out.println("消费了由TopicExchange交换机分配给topic.queue2队列的数据：" + msg);
    }

    @RabbitListener(queues = "object.queue")
    public void listenObjectQueue(Map<String, Object> map) {
        System.out.println(map);
    }
}
