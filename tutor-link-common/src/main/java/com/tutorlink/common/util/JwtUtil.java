package com.tutorlink.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, int role, String nickname) {
        return generateToken(userId, role, nickname, accessTokenExpiry);
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, null, null, refreshTokenExpiry);
    }

    private String generateToken(Long userId, Integer role, String nickname, long expiryMs) {
        Map<String, Object> claims = Map.of(
                "sub", userId.toString(),
                "role", role != null ? role : 0,
                "nickname", nickname != null ? nickname : ""
        );
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public int getRoleFromToken(String token) {
        return parseToken(token).get("role", Integer.class);
    }
}
