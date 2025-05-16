package com.example.ibank.accounts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Stream;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value( "${spring.security.oauth2.client.registration.paysrv.client-id}")
    private String serviceRegistrationId;

    // Роль "Любая роль" - добавляется пользователю если у него есть какая-либо роль в сервисе
    // (в нижнем регистре чтобы не пересекаться с ролями с сервера, которые переводятся в верхний регистр)
    final String ANY_ROLE = "any";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http
    ) {
        return http
            .csrf( ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange( exchanges -> exchanges
                .pathMatchers( "/actuator/health").permitAll()
                .anyExchange().hasRole( ANY_ROLE)
            )
            .oauth2ResourceServer( oauth2 -> oauth2
                .jwt( jwtSpec -> {
                    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter( jwt ->
                        Optional.ofNullable( jwt.getClaimAsMap( "resource_access"))
                            .map( ra -> ra.get( serviceRegistrationId))
                            .filter( Map.class::isInstance)
                            .map( Map.class::cast)
                            .map( res -> res.get( "roles"))
                            .filter( List.class::isInstance)
                            .map( List.class::cast)
                            .map( rawRoles ->
                                Stream.concat(
                                    Stream.of( ANY_ROLE).filter( r -> ! rawRoles.isEmpty()),
                                    Stream.of( rawRoles.toArray())
                                        .filter( String.class::isInstance)
                                        .map( String.class::cast)
                                        .map( role -> role.replace("-", "_").toUpperCase())
                                )
                                .toList()
                            )
                            .map( roles -> Flux.fromIterable( roles)
                                .map( role -> (GrantedAuthority) new SimpleGrantedAuthority( "ROLE_" + role))
                            )
                            .orElse( Flux.empty())
                    );
                    jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter);
                })
            )
            .build();
    }
}
