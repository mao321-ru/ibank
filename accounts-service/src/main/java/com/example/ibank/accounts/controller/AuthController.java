package com.example.ibank.accounts.controller;

import com.example.ibank.accounts.api.AuthApi;
import com.example.ibank.accounts.model.*;

import com.example.ibank.accounts.service.AuthService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.InvalidCredentialsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    @PreAuthorize( "hasRole('AUTH')")
    public Mono<ResponseEntity<AuthResponse>> validate(
        Mono<ValidateRequest> validateRequest,
        ServerWebExchange exchange
    ) {
        log.debug( "validate: ...");
        return validateRequest
            .flatMap( authService::validate)
            .map(ResponseEntity::ok)
            .onErrorResume(
                IllegalArgumentException.class,
                ex -> Mono.just( ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
            )
            .switchIfEmpty( Mono.just( ResponseEntity.notFound().build()));
    }

    @Override
    @PreAuthorize( "hasRole('AUTH')")
    public Mono<ResponseEntity<RegisterResponse>> register(
        Mono<RegisterRequest> registerRequest,
        ServerWebExchange exchange
    )
    {
        log.debug( "register: ...");
        return registerRequest
            .flatMap( authService::register)
            .map( resp -> ResponseEntity.status( HttpStatus.CREATED).body( resp))
            .switchIfEmpty( Mono.just(
                ResponseEntity.status( HttpStatus.CONFLICT).build()
            ));
    }

    @Override
    public Mono<ResponseEntity<Void>> changePassword(Mono<ChangePasswordRequest> changePasswordRequest, ServerWebExchange exchange) {
        log.debug( "changePassword: ...");
        return changePasswordRequest
            .flatMap( authService::changePassword)
            .map( isOk -> ResponseEntity.status( isOk ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND).build())
        ;
    }

}
