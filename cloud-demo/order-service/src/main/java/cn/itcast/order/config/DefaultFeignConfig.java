package cn.itcast.order.config;

import cn.itcast.order.fallback.UserClientFallBackFactory;
import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.BASIC;
    }

    @Bean
    public UserClientFallBackFactory userClientFallBackFactory(){
        return new UserClientFallBackFactory();
    }
}
