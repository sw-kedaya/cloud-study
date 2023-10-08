package cn.itcast.mq.spring;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAmqpTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage2SimpleQueue() throws InterruptedException {
        String message = "hello, spring amqp!";
        // 2.创建类并用uuid设置不同id
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        // 3.设置callback
        correlationData.getFuture().addCallback(result -> {
            if (result.isAck()) {
                log.debug("消息发送成功，ID：{}", correlationData.getId());
            } else {
                log.error("消息发送失败，ID：{}，原因{}", correlationData.getId(), result.getReason());
            }
        }, ex -> {
            log.error("消息发送异常，ID：{}，原因{}", correlationData.getId(), ex.getMessage());
        });

        // 1.在发送的形参里最后新增一个correlationData
        rabbitTemplate.convertAndSend("camq.topic", "simple.test", message, correlationData);
    }

    @Test
    public void testDurable() {
        Message message = MessageBuilder.withBody("test my set durable".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        rabbitTemplate.convertAndSend("simple.queue", message);
    }

    @Test
    public void testTTL() {
        Message message = MessageBuilder.withBody("hello ttl".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setExpiration("5000")
                .build();
        rabbitTemplate.convertAndSend("ttl.queue", message);
    }
}
