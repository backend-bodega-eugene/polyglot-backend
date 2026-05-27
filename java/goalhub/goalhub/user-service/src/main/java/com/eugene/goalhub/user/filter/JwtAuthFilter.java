package com.eugene.goalhub.user.filter;

import utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    //private final JwtUtil jwtUtil;

    /**
     * 白名单
     */
    private static final List<String> WHITE_LIST = List.of(
            "/user/register",
            "/user/login",

            // swagger
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    /**
     * Spring 路径匹配器
     */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

//    public JwtAuthFilter(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 调试时可以打开
        // System.out.println("request path = " + path);

        /**
         * 白名单放行
         */
        if (isWhiteList(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        /**
         * 获取 Authorization
         */
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write("""
                    {
                        "code":401,
                        "message":"未登录或token缺失",
                        "data":null
                    }
                    """);

            return;
        }

        /**
         * 提取 token
         */
        String token = authorization.substring(7);

        try {

            Claims claims = JwtUtil.userParseToken(token);

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            /**
             * 放入 request
             */
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            filterChain.doFilter(request, response);

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write("""
                    {
                        "code":401,
                        "message":"token无效或已过期",
                        "data":null
                    }
                    """);
        }
    }

    /**
     * 判断是否白名单
     */
    private boolean isWhiteList(String path) {

        return WHITE_LIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}