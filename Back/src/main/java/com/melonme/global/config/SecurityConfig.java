package com.melonme.global.config;

import com.melonme.global.security.JwtAccessDeniedHandler;
import com.melonme.global.security.JwtAuthenticationEntryPoint;
import com.melonme.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // Public: Test endpoints (local profile only)
                .requestMatchers("/api/test/**").permitAll()

                // Public: Auth endpoints
                .requestMatchers("/api/auth/kakao/callback", "/api/auth/google/callback").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()

                // Admin only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // MEMBER or ADMIN: write operations (posts, comments, scraps, likes, reports, blocks)
                .requestMatchers(HttpMethod.POST, "/api/posts").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/posts/**").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/posts/*/comments").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/posts/*/comments/*/replies").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/posts/*/scraps").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/likes").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/reports").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/blocks/**").hasAnyRole("MEMBER", "ADMIN")

                // Authenticated: all other API requests
                .requestMatchers("/api/**").authenticated()

                // Everything else
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
