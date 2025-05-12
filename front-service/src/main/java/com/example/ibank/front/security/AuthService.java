package com.example.ibank.front.security;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
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

    public Mono<AuthResponse> authenticate(String login, String password) {
        log.debug( "authenticate: login: {}", login);
        return accountsWebClient
            .post()
            .uri("/auth/validate")
            .header( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue( new AuthRequest(login, password))
            .retrieve()
            .bodyToMono( AuthResponse.class)
            .timeout( Duration.ofSeconds(3))
            .onErrorResume(e -> {
                log.debug( "Error on validate for [{}]: {}", login, e.getMessage());
                return Mono.just( new AuthResponse(false, null, List.of()));
            });
    }
}