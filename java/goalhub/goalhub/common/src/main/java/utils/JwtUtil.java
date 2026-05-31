package utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成和解析工具。
 */
public class JwtUtil {

    /**
     * 用户端 JWT 签名密钥。
     */
    private static final String USER_SECRET = "goalhub-user-service-secret-key-goalhub-2026";

    /**
     * 管理端 JWT 签名密钥。
     */
    private static final String ADMIN_SECRET = "goalhub-admin-service-secret-key-goalhub-2026";

    /**
     * 用户端 JWT 过期时间，单位毫秒。
     */
    private static final long USER_EXPIRE_TIME = 1000 * 60 * 60 * 24L;

    /**
     * 管理端 JWT 过期时间，单位毫秒。
     */
    private static final long ADMIN_EXPIRE_TIME = 1000 * 60 * 60 * 12L;

    /**
     * 生成用户端 JWT。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return JWT 字符串
     */
    public static String userGenerateToken(Long userId, String username) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + USER_EXPIRE_TIME);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(Keys.hmacShaKeyFor(USER_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * 解析用户端 JWT。
     *
     * @param token JWT 字符串
     * @return JWT Claims
     */
    public static Claims userParseToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(USER_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 生成管理端 JWT。
     *
     * @param adminId  管理员 ID
     * @param username 管理员账号
     * @param role     管理员角色
     * @return JWT 字符串
     */
    public static String adminGenerateToken(Long adminId, String username, String role) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + ADMIN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(Keys.hmacShaKeyFor(ADMIN_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * 解析管理端 JWT。
     *
     * @param token JWT 字符串
     * @return JWT Claims
     */
    public static Claims adminParseToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(ADMIN_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
