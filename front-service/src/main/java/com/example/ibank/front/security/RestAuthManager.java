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
        String login = authentication.getName();
        String password = authentication.getCredentials().toString();
        return authService.authenticate(login, password)
                .switchIfEmpty( Mono.error( new BadCredentialsException("Invalid credentials")))
                .map(resp -> new UsernamePasswordAuthenticationToken(
                        AuthUser.builder()
                                .login( resp.getLogin())
                                .userName( resp.getUserName())
                                .birthDate( resp.getBirthDate())
                                .build(),
                        null,
                        resp.getRoles().stream()
                                .map( SimpleGrantedAuthority::new)
                                .collect(Collectors.toList())
                ));
    }
}