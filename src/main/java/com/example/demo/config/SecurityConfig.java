package com.example.demo.config;

import com.example.demo.config.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // khi chương trình runtime, Spring sẽ quét class này và tạo bean bên trong
@EnableWebSecurity // bật security (Spring Security)
@EnableMethodSecurity(prePostEnabled = true) // 🔥 dùng @PreAuthorize // @EnableGlobalMethodSecurity đã bị deprecated
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                // ❌ REST API không cần CSRF
                .csrf(csrf -> csrf.disable())

                // ❌ Không dùng session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                        )
                )

                // ❌ Không dùng login form & basic auth
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ Phân quyền
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("USER")

                        .anyRequest().authenticated()
                )

                // 🔥 Gắn JWT filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
