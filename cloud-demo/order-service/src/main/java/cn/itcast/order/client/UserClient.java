package cn.itcast.order.client;

import cn.itcast.order.config.DefaultFeignConfig;
import cn.itcast.order.fallback.UserClientFallBackFactory;
import cn.itcast.order.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(value = "userservice", configuration = DefaultFeignConfig.class)
@FeignClient(value = "userservice", fallbackFactory = UserClientFallBackFactory.class)
public interface UserClient {

    @GetMapping("/user/{id}")
    User findById(@PathVariable Long id);
}
