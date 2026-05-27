package com.eugene.goalhub.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@Component
public class GatewayJwtAuthFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String ADMIN_PREFIX = "/admin/";

    private static final List<String> WHITE_LIST = List.of(
            "/api/user/login",
            "/api/user/register",

            "/admin/auth/login",
            "/admin/test/ping",

            // swagger
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/user/swagger-ui/**",
            "/api/user/v3/api-docs/**",
            "/api/soccer/swagger-ui/**",
            "/api/soccer/v3/api-docs/**",
            "/admin/swagger-ui/**",
            "/admin/v3/api-docs/**"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        if (path.startsWith(ADMIN_PREFIX)) {
            return handleAdminToken(exchange, chain);
        }

        return handleUserToken(exchange, chain);
    }

    private Mono<Void> handleUserToken(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            String token = getBearerToken(exchange);

            Claims claims = JwtUtil.userParseToken(token);

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            ServerHttpRequest newRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> handleAdminToken(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            String token = getBearerToken(exchange);

            Claims claims = JwtUtil.adminParseToken(token);

            String adminId = claims.getSubject();
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            ServerHttpRequest newRequest = exchange.getRequest()
                    .mutate()
                    .header("X-Admin-Id", adminId)
                    .header("X-Admin-Username", username)
                    .header("X-Admin-Role", role == null ? "" : role)
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private String getBearerToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Missing token");
        }

        return authorization.substring(7);
    }

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

    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}