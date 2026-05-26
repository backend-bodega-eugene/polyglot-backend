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

    private static final List<String> WHITE_LIST = List.of(
            "/api/user/login",
            "/api/user/register",

            // swagger
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/user/swagger-ui/**",
            "/api/user/v3/api-docs/**",
            "/api/soccer/swagger-ui/**",
            "/api/soccer/v3/api-docs/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            try {

                Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED);

                String body = new ObjectMapper().writeValueAsString(result);

                DataBuffer buffer = exchange.getResponse()
                        .bufferFactory()
                        .wrap(body.getBytes(StandardCharsets.UTF_8));

                return exchange.getResponse().writeWith(Mono.just(buffer));

            } catch (Exception e) {
                return exchange.getResponse().setComplete();
            }
        }

        String token = authorization.substring(7);

        try {

            Claims claims = JwtUtil.parseToken(token);

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            ServerHttpRequest newRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .build();

            ServerWebExchange newExchange = exchange.mutate()
                    .request(newRequest)
                    .build();

            return chain.filter(newExchange);

        } catch (Exception e) {

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            try {

                Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED);

                String body = new ObjectMapper().writeValueAsString(result);

                DataBuffer buffer = exchange.getResponse()
                        .bufferFactory()
                        .wrap(body.getBytes(StandardCharsets.UTF_8));

                return exchange.getResponse().writeWith(Mono.just(buffer));

            } catch (Exception ex) {
                return exchange.getResponse().setComplete();
            }
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