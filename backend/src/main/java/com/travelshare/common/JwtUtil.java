package com.travelshare.common;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 签发与校验（jjwt，HS256）。payload 只含 userId 与 role，无 refresh token。
 */
@Component
public class JwtUtil {

    private final JwtProperties properties;
    private Key signingKey;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        String secret = properties.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret 长度不足 32 字节，无法用于 HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String role) {
        return generateToken(userId, role, properties.getExpireHours() * 3600_000L);
    }

    /**
     * 指定存活时长签发。仅测试用它构造过期/临期 token，业务代码一律走两参数重载。
     */
    public String generateToken(Long userId, String role, long ttlMillis) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ttlMillis))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 校验签名与有效期后返回登录身份；任何不合法 token 抛 {@code JwtException}/{@code IllegalArgumentException}。
     */
    public LoginUser parse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Long userId = Long.valueOf(String.valueOf(claims.get("userId")));
        String role = String.valueOf(claims.get("role"));
        return new LoginUser(userId, role);
    }
}
