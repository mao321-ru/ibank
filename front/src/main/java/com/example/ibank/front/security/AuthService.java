package com.example.ibank.front.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final WebClient accountsWebClient;

    public Mono<AuthResponse> authenticate(String login, String password) {
        log.debug( "authenticate: login: {}, password: {}", login, password);
        return accountsWebClient
            .post()
            .uri("/api/auth/validate")
            .bodyValue( new AuthRequest(login, password))
            .retrieve()
            .bodyToMono( AuthResponse.class)
            .timeout( Duration.ofSeconds(3))
            .onErrorResume(e -> Mono.just(
                new AuthResponse(false, null, List.of())
            ))
        ;
    }
}