package com.example.ibank.accounts.controller;

import com.example.ibank.accounts.api.UserApi;
import com.example.ibank.accounts.model.*;

import com.example.ibank.accounts.service.UserService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@PreAuthorize( "hasRole('AUTH')")
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public Mono<ResponseEntity<Flux<UserShort>>> listUsers(ServerWebExchange exchange) {
        log.debug( "listUsers: ...");
        return Mono.just( ResponseEntity.ok( userService.getAllUsers()));
    }

    @Override
    public Mono<ResponseEntity<UserInfo>> createUser(
            Mono<UserCreate> registerRequest,
            ServerWebExchange exchange
    )
    {
        log.debug( "createUser: ...");
        return registerRequest
                .flatMap( userService::createUser)
                .map( resp -> ResponseEntity.status( HttpStatus.CREATED).body( resp))
                .switchIfEmpty( Mono.just(
                        ResponseEntity.status( HttpStatus.CONFLICT).build()
                ));
    }

    @Override
    public Mono<ResponseEntity<UserAccounts>> getUserAccounts(String login, ServerWebExchange exchange) {
        return userService.getUserAccounts(login)
            .map( ResponseEntity::ok)
            .defaultIfEmpty( ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<UserInfo>> validate(
        String login,
        Mono<ValidateRequest> validateRequest,
        ServerWebExchange exchange
    ) {
        log.debug( "validate: login: {}", login);
        return validateRequest
            .flatMap( rq -> userService.validate( login, rq.getPassword()))
            .map(ResponseEntity::ok)
            .onErrorResume(
                IllegalArgumentException.class,
                ex -> Mono.just( ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
            )
            .switchIfEmpty( Mono.just( ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> changePassword(
        String login,
        Mono<ChangePasswordRequest> changePasswordRequest,
        ServerWebExchange exchange
    ) {
        log.debug( "changePassword: login: {}", login);
        return changePasswordRequest
            .flatMap( rq -> userService.changePassword( login, rq.getPassword()))
            .map( isOk -> ResponseEntity.status( isOk ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND).build())
        ;
    }

}
