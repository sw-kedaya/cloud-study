package cn.itcast.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(-1)
@Component
public class AuthorizeFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求参数
        MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();

        //从参数中取出值(根据请求头的key来查出值)
        String authorization = params.getFirst("authorization");

        //如果该值为admin。则放行
        if ("admin".equals(authorization)) {
            return chain.filter(exchange);
        }
        //否则，给用户反馈并拦截(401是未登录的状态码)
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
