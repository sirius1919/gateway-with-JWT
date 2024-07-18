package com.mcsirius.cloud.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.mcsirius.cloud.config.AuthProperties;
import com.mcsirius.cloud.exp.UnauthorizedException;
import com.mcsirius.cloud.utils.JwtTool;
import com.mcsirius.cloud.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.mcsirius.cloud.contants.AuthContents.AUTH_TOKEN_BEGIN;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final JwtTool jwtTool;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取request
        ServerHttpRequest request = exchange.getRequest();
        // 2. 判断是否需要登录拦截
        if (isExcluded(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        // 3. 获取token
        String authToken = null;
        List<String> authTokens = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authTokens != null && !authTokens.isEmpty()) {
            authToken = authTokens.get(0);
        }
        if (authToken != null && authToken.startsWith(AUTH_TOKEN_BEGIN)) {
            authToken = authToken.substring(7);
        } else {
            return unauthorized(exchange);
        }
        // 4. 校验并解析token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(authToken);
        } catch (UnauthorizedException e) {
            return unauthorized(exchange);
        }
        // 5. 判断token是否在redis中
        UserLoginVO userLoginVO;
        if (Boolean.TRUE.equals(redisTemplate.hasKey("user:" + userId))) {
            userLoginVO = (UserLoginVO) redisTemplate.opsForValue().get("user:"+userId);
        } else {
            return unauthorized(exchange);
        }
        // 6. 传递用户信息
        if(userLoginVO != null) {
            exchange.mutate().request(builder -> {builder.header("userId", String.valueOf(userLoginVO.getId()));
            }).build();
        } else {
            return unauthorized(exchange);
        }
        // 7. 放行
        return chain.filter(exchange);
    }

    // 拦截，设置响应状态码为401
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private boolean isExcluded(String path) {
        for (String excludePath: authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
