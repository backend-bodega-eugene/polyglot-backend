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

/**
 * 用户服务 JWT 认证过滤器。
 * <p>
 * 校验非白名单请求的用户 token，并把用户身份写入 request attribute。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    //private final JwtUtil jwtUtil;

    /**
     * 不需要 JWT 鉴权的路径列表。
     */
    private static final List<String> WHITE_LIST = List.of(
            "/user/register",
            "/user/login",
            "/internal/admin/users/**",
            "/internal/admin/account/**",
            // Swagger/OpenAPI 文档路径。
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    /**
     * Spring 路径匹配器。
     */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

//    public JwtAuthFilter(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }

    /**
     * 过滤每个 HTTP 请求，白名单直接放行，其余请求校验 Bearer token。
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 调试时可以打开
        // System.out.println("request path = " + path);

        // 白名单请求直接放行。
        if (isWhiteList(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从 Authorization 请求头中读取 Bearer token。
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

        // 提取 Bearer token 内容。
        String token = authorization.substring(7);

        try {

            Claims claims = JwtUtil.userParseToken(token);

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);

            // 将用户身份放入 request attribute，供后续业务读取。
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
     * 判断请求路径是否在白名单中。
     *
     * @param path 请求路径
     * @return true 表示无需鉴权
     */
    private boolean isWhiteList(String path) {

        return WHITE_LIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}
