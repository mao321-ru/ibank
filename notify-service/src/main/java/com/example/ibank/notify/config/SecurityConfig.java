package com.example.ibank.notify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http
    ) {
        return http
            .authorizeExchange( exchanges -> exchanges
                .pathMatchers(
                    "/actuator/health",
                    "/actuator/health/*"
                ).permitAll()
                .anyExchange().denyAll()
            )
            .build();
    }
}
