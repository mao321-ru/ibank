package com.example.ibank.accounts.config;

import com.example.ibank.shared.resourceserver.security.KeycloakJwtGrantedAuthoritiesConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value( "${spring.security.oauth2.client.registration.ibank-service.client-id}")
    private String serviceRegistrationId;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http
    ) {
        return http
            .csrf( ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange( exchanges -> exchanges
                .pathMatchers( "/actuator/**").permitAll()
                .anyExchange().hasRole( KeycloakJwtGrantedAuthoritiesConverter.ANY_ROLE)
            )
            .oauth2ResourceServer( oauth2 -> oauth2
                .jwt( jwtSpec -> {
                    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                        new KeycloakJwtGrantedAuthoritiesConverter( serviceRegistrationId)
                    );
                    jwtSpec.jwtAuthenticationConverter( jwtAuthenticationConverter);
                })
            )
            .build();
    }
}
