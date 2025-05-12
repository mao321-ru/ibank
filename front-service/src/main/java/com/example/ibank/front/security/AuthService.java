package com.example.ibank.front.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    public Mono<AuthResponse> authenticate(String username, String password) {
        log.debug( "authenticate: username: {}", username);
        return accountsWebClient
            .post()
            .uri("/auth/validate")
            .header( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue( new AuthRequest(username, password))
            .retrieve()
            .bodyToMono( AuthResponse.class)
            .timeout( Duration.ofSeconds(3))
            .onErrorResume(e -> {
                log.debug( "Error on validate for [{}]: {}", username, e.getMessage());
                return Mono.just( new AuthResponse(null, List.of()));
            });
    }
}