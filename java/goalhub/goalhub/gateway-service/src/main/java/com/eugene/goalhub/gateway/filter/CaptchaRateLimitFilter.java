package com.eugene.goalhub.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import response.Result;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * 验证码接口限流过滤器。
 */
@Component
public class CaptchaRateLimitFilter implements GlobalFilter, Ordered {

    private static final List<String> CAPTCHA_PATHS = List.of(
            "/api/user/captcha",
           "/api/user/forgotpassword/sendcode",
            "/api/user/forgotpassword/reset"
           // "/admin/auth/captcha"
    );

    private static final int MINUTE_LIMIT = 10;
    private static final int HOUR_LIMIT = 60;

    private final ReactiveStringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CaptchaRateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (!CAPTCHA_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        String ip = getClientIp(exchange.getRequest());

        String minuteKey = "rate:captcha:ip:" + ip + ":minute";
        String hourKey = "rate:captcha:ip:" + ip + ":hour";

        return checkLimit(minuteKey, MINUTE_LIMIT, Duration.ofMinutes(1))
                .flatMap(minutePassed -> {
                    if (!minutePassed) {
                        return tooManyRequests(exchange);
                    }

                    return checkLimit(hourKey, HOUR_LIMIT, Duration.ofHours(1))
                            .flatMap(hourPassed -> {
                                if (!hourPassed) {
                                    return tooManyRequests(exchange);
                                }

                                return chain.filter(exchange);
                            });
                });
    }

    private Mono<Boolean> checkLimit(String key, int limit, Duration expire) {
        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(key, expire)
                                .thenReturn(count <= limit);
                    }

                    return Mono.just(count <= limit);
                });
    }

    private String getClientIp(ServerHttpRequest request) {

        String ip = request.getHeaders().getFirst("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeaders().getFirst("X-Real-IP");

        if (ip != null && !ip.isBlank()) {
            return ip;
        }

        if (request.getRemoteAddress() != null
                && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            Result<Void> result = Result.fail(429, "验证码请求过于频繁，请稍后再试");

            String body = objectMapper.writeValueAsString(result);

            DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));

            return exchange.getResponse().writeWith(Mono.just(buffer));

        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -200;
    }
}