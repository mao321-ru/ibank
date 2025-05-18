package com.example.ibank.shared.resourceserver.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Flux<GrantedAuthority>> {

    // Роль "Любая роль" - добавляется пользователю если у него есть какая-либо роль в сервисе
    // (в нижнем регистре чтобы не пересекаться с ролями с сервера, которые переводятся в верхний регистр)
    public final static String ANY_ROLE = "any";

    private final String serviceRegistrationId;

    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        // Получение ролей из "resource_access.{client}.roles"
        return Optional.ofNullable( jwt.getClaimAsMap( "resource_access"))
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
            .orElse( Flux.empty());
    }
}
