package com.tutorlink.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.web.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    ApiResult.error(ResultCode.FORBIDDEN)));
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    ApiResult.error(ResultCode.UNAUTHORIZED)));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/subjects/**").permitAll()
                        .requestMatchers("/api/v1/areas/**").permitAll()
                        .requestMatchers("/api/v1/tutors", "/api/v1/tutors/{userId}", "/api/v1/tutors/{userId}/**").permitAll()
                        .requestMatchers("/api/v1/users/{id}/basic").permitAll()
                        .requestMatchers("/api/v1/payments/wechat-callback").permitAll()
                        .requestMatchers("/api/v1/payments/refund-callback").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                        // AI 推荐问题（公开）
                        .requestMatchers(HttpMethod.GET, "/api/v1/ai/suggestions").permitAll()
                        // 上传文件静态资源
                        .requestMatchers("/uploads/**").permitAll()
                        // API 文档
                        .requestMatchers("/doc.html", "/v3/api-docs/**", "/swagger-ui/**", "/webjars/**").permitAll()
                        // WebSocket 端点
                        .requestMatchers("/ws/**").permitAll()
                        // 管理员接口
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
