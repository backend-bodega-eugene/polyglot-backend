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
 *
 * <p>
 * 针对验证码和找回密码相关接口按客户端 IP 做分钟级和小时级限流，
 * 避免验证码发送、验证或重置入口被高频调用。
 * </p>
 */
@Component
public class CaptchaRateLimitFilter implements GlobalFilter, Ordered {

    /**
     * 需要执行验证码限流的网关路径。
     */
    private static final List<String> CAPTCHA_PATHS = List.of(
            "/api/user/captcha",
            "/api/user/forgotpassword/sendcode",
            "/api/user/forgotpassword/reset"
            // "/admin/auth/captcha"
    );

    /**
     * 单个客户端 IP 每分钟允许访问验证码相关接口的最大次数。
     */
    private static final int MINUTE_LIMIT = 10;

    /**
     * 单个客户端 IP 每小时允许访问验证码相关接口的最大次数。
     */
    private static final int HOUR_LIMIT = 60;

    /**
     * 响应式 Redis 字符串操作模板，用于记录限流计数。
     */
    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * JSON 序列化工具，用于写出限流失败响应。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建验证码接口限流过滤器。
     *
     * @param redisTemplate 响应式 Redis 字符串操作模板
     */
    public CaptchaRateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 执行验证码接口限流检查。
     *
     * <p>非验证码限流路径直接放行；命中限流路径时先检查分钟限流，再检查小时限流。</p>
     *
     * @param exchange 当前网关请求上下文
     * @param chain    网关过滤器链
     * @return 过滤处理结果
     */
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

    /**
     * 检查指定 Redis 计数 key 是否超过限流阈值。
     *
     * <p>第一次创建计数 key 时设置过期时间，后续请求只递增并比较阈值。</p>
     *
     * @param key    Redis 限流计数 key
     * @param limit  限流阈值
     * @param expire 计数窗口过期时间
     * @return 未超过阈值返回 true，否则返回 false
     */
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

    /**
     * 获取客户端真实 IP。
     *
     * <p>优先读取 CDN 和反向代理透传头，无法获取时回退到远端地址。</p>
     *
     * @param request 当前 HTTP 请求
     * @return 客户端 IP，无法识别时返回 unknown
     */
    private String getClientIp(ServerHttpRequest request) {

        String ip = request.getHeaders().getFirst("CF-Connecting-IP");

        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }

        ip = request.getHeaders().getFirst("X-Real-IP");

        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }

        ip = request.getHeaders().getFirst("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        if (request.getRemoteAddress() != null
                && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * 写出验证码请求过于频繁的响应。
     *
     * @param exchange 当前网关请求上下文
     * @return 响应写出结果
     */
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

    /**
     * 获取过滤器执行顺序。
     *
     * @return 过滤器顺序，数值越小优先级越高
     */
    @Override
    public int getOrder() {
        return -200;
    }
}
