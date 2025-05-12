package com.example.ibank.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http
    ) {
        return http
            .csrf( ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange( exchanges -> exchanges
                .pathMatchers( "/actuator/health").permitAll()
                .pathMatchers( "/auth/validate").permitAll()
                .anyExchange().authenticated()
            )
            .build();
    }
}