package com.example.demo.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public Duration getAccessTokenExpiration() {
        return Duration.ofMillis(jwtProperties.getAccessTokenExpiration());
    }

    public Duration getRefreshTokenExpiration() {
        return Duration.ofMillis(jwtProperties.getRefreshTokenExpiration());
    }

    public Duration getVerifyTokenExpiration() {
        return Duration.ofMillis(jwtProperties.getVerifyTokenExpiration());
    }
    private Key signingKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // =========================
    // 🔐 ACCESS TOKEN
    // =========================
    public String generateAccessToken(String userId, List<String> roles) {
        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtProperties.getAccessTokenExpiration()))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // 🔁 REFRESH TOKEN
    // =========================
    public String generateRefreshToken(String userId) {
        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration()))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // ✉️ VERIFY EMAIL TOKEN
    // =========================
    public String generateVerifyToken(String userId) {
        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim("purpose", "verify")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtProperties.getVerifyTokenExpiration()))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // 🔐 RESET PASSWORD TOKEN (🆕)
    // =========================
    public String generateResetPasswordToken(String email) {
        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(email)
                .claim("purpose", "reset-password")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 10 * 60 * 1000)) // 10 phút
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // 📤 EXTRACT
    // =========================
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return extractClaims(token).getId();
    }

    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }

    public String extractPurpose(String token) {
        return extractClaims(token).get("purpose", String.class);
    }

    // =========================
    // ⏱️ TTL
    // =========================
    public long getRemainingTime(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public Duration getRemainingDuration(String token) {
        return Duration.ofMillis(getRemainingTime(token));
    }



    // =========================
    // ✅ VALIDATE
    // =========================
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================
    // 🔍 PARSE
    // =========================
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
