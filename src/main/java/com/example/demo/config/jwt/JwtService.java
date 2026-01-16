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
import java.util.UUID;

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
    // 🔐 ACCESS TOKEN
    // =========================
    public String generateAccessToken(String userId, List<String> roles) {
        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())   // ✅ jti
                .setSubject(userId)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(
                        new Date(now.getTime() + jwtProperties.getAccessTokenExpiration())
                )
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // 🔁 REFRESH TOKEN
    // =========================
    public String generateRefreshToken(String userId) {
        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())   // ✅ jti
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(
                        new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration())
                )
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // ✅ VERIFY / OTP TOKEN
    // =========================
    public String generateVerifyToken(String userId) {
        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim("purpose", "verify")
                .setIssuedAt(now)
                .setExpiration(
                        new Date(now.getTime() + jwtProperties.getVerifyTokenExpiration())
                )
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public long getVerifyTokenExpiration() {
        return jwtProperties.getVerifyTokenExpiration();
    }

    // =========================
    // 📤 EXTRACT
    // =========================
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return extractClaims(token).getId();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }

    // Extract custom "purpose" claim (e.g., "verify")
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
    public long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshTokenExpiration();
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
