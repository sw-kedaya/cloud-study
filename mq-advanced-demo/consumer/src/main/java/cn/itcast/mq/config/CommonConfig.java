package cn.itcast.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("simple.direct", true, false);
    }

    @Bean
    public Queue directQueue(){
        return QueueBuilder.durable("simple.queue").build();
    }
}
