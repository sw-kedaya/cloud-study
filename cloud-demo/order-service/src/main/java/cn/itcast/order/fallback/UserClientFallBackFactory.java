package cn.itcast.order.fallback;

import cn.itcast.order.client.UserClient;
import cn.itcast.order.pojo.User;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserClientFallBackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable throwable) {
        return new UserClient() {
            @Override
            public User findById(Long id) {
                log.error("查询用户失败" + throwable);
                return new User();
            }
        };
    }
}
