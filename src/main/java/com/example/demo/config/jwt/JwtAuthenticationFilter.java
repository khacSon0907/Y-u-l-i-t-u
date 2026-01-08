package com.example.demo.config.jwt;

import com.example.demo.service.redis.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1️⃣ Lấy Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2️⃣ Không có token → cho qua
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Lấy token
        String token = authHeader.substring(7);

        if(redisService.isAccessTokenBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 4️⃣ Validate token
        if (!jwtService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5️⃣ Lấy userId + roles từ JWT
        String userId = jwtService.extractUserId(token);
        List<String> roles = jwtService.extractRoles(token);

        // 6️⃣ Chưa authenticate
        if (userId != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Convert role → GrantedAuthority
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // 7️⃣ Tạo Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,          // principal
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 8️⃣ Set vào SecurityContext
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        // 9️⃣ Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}
