package com.example.demo.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // 🔑 Key ký JWT
    private Key signingKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // =========================
    // 🔐 ACCESS TOKEN (có ROLE)
    // =========================
    public String generateAccessToken(String userId, List<String> roles) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(userId)                 // userId ổn định
                .claim("roles", roles)              // phân quyền
                .setIssuedAt(now)
                .setExpiration(
                        new Date(now.getTime() + jwtProperties.getAccessTokenExpiration())
                )
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // 🔁 REFRESH TOKEN (không role)
    // =========================
    public String generateRefreshToken(String userId) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(
                        new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration())
                )
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // 📤 EXTRACT DATA
    // =========================
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }

    // =========================
    // ✅ VALIDATE TOKEN
    // =========================
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshTokenExpiration();
    }


    // Calculate token expiration time in milliseconds
    public long getRemainingTime(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }


    // =========================
    // 🔍 PARSE JWT
    // =========================
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
