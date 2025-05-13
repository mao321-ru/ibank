package com.example.ibank.front.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestAuthManager implements ReactiveAuthenticationManager {

    private final AuthService authService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        return authService.authenticate(username, password)
                .switchIfEmpty( Mono.error( new BadCredentialsException("Invalid credentials")))
                .map(authResponse -> new UsernamePasswordAuthenticationToken(
                        authResponse.getUserId(),
                        null,
                        authResponse.getRoles().stream()
                                .map( SimpleGrantedAuthority::new)
                                .collect(Collectors.toList())
                ));
    }
}