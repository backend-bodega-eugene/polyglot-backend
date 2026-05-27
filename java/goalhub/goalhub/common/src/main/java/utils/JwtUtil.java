package utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String USER_SECRET = "goalhub-user-service-secret-key-goalhub-2026";
    private static final String ADMIN_SECRET = "goalhub-admin-service-secret-key-goalhub-2026";

    private static final long USER_EXPIRE_TIME = 1000 * 60 * 60 * 24L;
    private static final long ADMIN_EXPIRE_TIME = 1000 * 60 * 60 * 12L;

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

    public static Claims userParseToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(USER_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

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

    public static Claims adminParseToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(ADMIN_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}