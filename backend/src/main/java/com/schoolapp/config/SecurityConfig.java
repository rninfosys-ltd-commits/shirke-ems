
package com.schoolapp.config;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Value("${app.cors.allowed-origins:http://localhost:4200}")
        private String allowedOrigins;

        @Bean

        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                // Preflight
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // Public endpoints
                                                .requestMatchers(
                                                                "/error",
                                                                "/health",
                                                                "/api/auth/**",
                                                                "/api/public/**",
                                                                "/api/production/**",
                                                                "/api/material-master/**",
                                                                "/api/workflow/**",
                                                                "/api/reports/**",
                                                                "/api/casting-report/**",
                                                                "/api/wire-cutting/**",
                                                                "/api/autoclave/**",
                                                                "/api/block-separating/**",
                                                                "/api/cube-test/**",
                                                                "/api/rejection/**",
                                                                "/api/batch-traceability/**",
                                                                "/api/km-batch/**",
                                                                "/api/km-entry/**",
                                                                "/api/receipts/**",
                                                                "/api/users/**",
                                                                "/api/party-prices/**",
                                                                "/api/leads/**",
                                                                "/api/projects/**",
                                                                "/api/inquiries/**",
                                                                "/api/inquiry-schedule/**",
                                                                "/api/location/**",
                                                                "/api/employees/**",
                                                                "/api/roots/**",
                                                                "/api/user-role-details/**",
                                                                "/api/customer-trn/**",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**")
                                                .permitAll()

                                                // Example public GET
                                                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                                                .requestMatchers("/api/batch/**").permitAll()
                                                .requestMatchers("/api/km-batch/**").permitAll()
                                                .requestMatchers("/api/km-entry/**").permitAll()

                                                // User
                                                .requestMatchers("/api/cart/**", "/api/orders/**")
                                                .hasRole("USER")

                                                .requestMatchers("/api/admin/**")
                                                .permitAll()

                                                .anyRequest().authenticated())

                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())

                                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                                                (request, response, exception) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("""
                                                                        {
                                                                          "error": "UNAUTHORIZED",
                                                                          "message": "Invalid or missing JWT token"
                                                                        }
                                                                        """);
                                                }));

                http.addFilterBefore(jwtAuthenticationFilter,
                                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public CorsFilter corsFilter() {
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowCredentials(true);
                config.setAllowedOriginPatterns(
                                List.of(allowedOrigins.split("\\s*,\\s*")));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setExposedHeaders(List.of("Authorization"));
                config.setMaxAge(3600L);
                source.registerCorsConfiguration("/**", config);
                return new CorsFilter(source);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }
}
