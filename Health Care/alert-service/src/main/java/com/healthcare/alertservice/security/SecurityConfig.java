package com.healthcare.alertservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/h2-console/**").permitAll()
                        // Threshold configuration is an administrative operation.
                        .requestMatchers(HttpMethod.POST, "/api/alert-rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/alert-rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/alert-rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/alerts/evaluate")
                            .hasAnyRole("ADMIN", "DOCTOR", "NURSE")
                        .requestMatchers(HttpMethod.PATCH, "/api/alerts/**")
                            .hasAnyRole("ADMIN", "DOCTOR", "NURSE")
                        .requestMatchers(HttpMethod.DELETE, "/api/alerts/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
