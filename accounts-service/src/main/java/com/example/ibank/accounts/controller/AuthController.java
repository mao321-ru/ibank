package com.example.ibank.accounts.controller;

import com.example.ibank.accounts.api.AuthApi;
import com.example.ibank.accounts.model.ValidateRequest;
import com.example.ibank.accounts.model.AuthResponse;

import com.example.ibank.accounts.service.AuthService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import org.apache.hc.client5.http.auth.InvalidCredentialsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public Mono<ResponseEntity<AuthResponse>> validate(
        Mono<ValidateRequest> validateRequest,
        ServerWebExchange exchange
    ) {
        return validateRequest
            .flatMap( authService::validate)
            .map(ResponseEntity::ok)
            .onErrorResume(
                IllegalArgumentException.class,
                ex -> Mono.just( ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
            )
            .switchIfEmpty( Mono.just( ResponseEntity.notFound().build()));
    }
}