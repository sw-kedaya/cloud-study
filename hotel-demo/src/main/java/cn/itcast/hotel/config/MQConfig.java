package cn.itcast.hotel.config;

import cn.itcast.hotel.constant.MQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(MQConstant.HOTEL_EXCHANGE, true, false);
    }

    @Bean
    public Queue insertQueue() {
        return new Queue(MQConstant.HOTEL_INSERT_QUEUE, true);
    }

    @Bean
    public Queue deleteQueue() {
        return new Queue(MQConstant.HOTEL_DELETE_QUEUE, true);
    }

    @Bean
    public Binding insertBinding(){
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(MQConstant.HOTEL_INSERT_KEY);
    }

    @Bean
    public Binding deleteBinding(){
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MQConstant.HOTEL_DELETE_KEY);
    }
}
