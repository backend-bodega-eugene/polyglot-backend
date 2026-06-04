package com.eugene.goalhub.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.BusinessException;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import response.Result;
import response.ResultCode;
import utils.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关 JWT 认证过滤器。
 * <p>
 * 负责在请求进入后端服务前校验 JWT，并把用户或管理员身份信息写入请求头。
 */
@Component
public class GatewayJwtAuthFilter implements GlobalFilter, Ordered {

    /**
     * Ant 风格路径匹配器，用于判断白名单路径。
     */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 后台管理接口路径前缀。
     */
    private static final String ADMIN_PREFIX = "/admin/";

    /**
     * 不需要 JWT 鉴权的路径列表。
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/user/login",
            "/api/user/register",
            "/api/user/captcha",
            "/admin/auth/login",
           // "/admin/test/ping",

            // Swagger/OpenAPI 文档路径。
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/user/swagger-ui/**",
            "/api/user/v3/api-docs/**",
            "/api/soccer/swagger-ui/**",
            "/api/soccer/v3/api-docs/**",
            "/admin/swagger-ui/**",
            "/admin/v3/api-docs/**"
    );

    /**
     * JSON 序列化工具，用于输出统一错误响应。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 网关全局过滤入口。
     *
     * @param exchange 当前请求上下文
     * @param chain    网关过滤链
     * @return 异步处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 白名单请求直接放行。
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 后台管理接口使用管理员 token，其余接口使用普通用户 token。
        if (path.startsWith(ADMIN_PREFIX)) {
            return handleAdminToken(exchange, chain);
        }

        return handleUserToken(exchange, chain);
    }

    /**
     * 校验普通用户 JWT，并透传用户身份请求头。
     *
     * @param exchange 当前请求上下文
     * @param chain    网关过滤链
     * @return 异步处理结果
     */
    private Mono<Void> handleUserToken(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            String token = getBearerToken(exchange);

            Claims claims = JwtUtil.userParseToken(token);

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            // 将解析出的用户身份写入请求头，供下游服务读取。
            ServerHttpRequest newRequest = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-Username");
                        headers.add("X-User-Id", userId);
                        headers.add("X-Username", username);
                    })
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    /**
     * 校验后台管理员 JWT，并透传管理员身份请求头。
     *
     * @param exchange 当前请求上下文
     * @param chain    网关过滤链
     * @return 异步处理结果
     */
    private Mono<Void> handleAdminToken(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            String token = getBearerToken(exchange);

            Claims claims = JwtUtil.adminParseToken(token);

            String adminId = claims.getSubject();
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            // 将解析出的管理员身份写入请求头，供 admin-service 读取。
            ServerHttpRequest newRequest = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove("X-Admin-Id");
                        headers.remove("X-Admin-Username");
                        headers.remove("X-Admin-Role");
                        headers.add("X-Admin-Id", adminId);
                        headers.add("X-Admin-Username", username);
                        headers.add("X-Admin-Role", role == null ? "" : role);
                    })
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    /**
     * 从 Authorization 请求头中提取 Bearer token。
     *
     * @param exchange 当前请求上下文
     * @return JWT 字符串
     */
    private String getBearerToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.MISS_TOKEN);
        }

        return authorization.substring(7);
    }

    /**
     * 返回统一的 401 未授权响应。
     *
     * @param exchange 当前请求上下文
     * @return 异步处理结果
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED);

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
     * 判断请求路径是否在白名单中。
     *
     * @param path 请求路径
     * @return true 表示无需鉴权
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    /**
     * 设置过滤器执行顺序，数值越小优先级越高。
     *
     * @return 过滤器顺序
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
