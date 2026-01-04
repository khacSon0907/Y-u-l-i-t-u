package com.example.demo.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1️⃣ Lấy header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2️⃣ Nếu không có token → bỏ qua
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Cắt token ra khỏi "Bearer "
        String token = authHeader.substring(7);

        // 4️⃣ Lấy subject (email / username)
        String subject = jwtService.extractSubject(token);

        // 5️⃣ Nếu chưa authenticate & token hợp lệ
        if (subject != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.validateToken(token)) {

            // 6️⃣ Load user từ DB (qua UserDetailsService)
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(subject);

            // 7️⃣ Tạo Authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
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
