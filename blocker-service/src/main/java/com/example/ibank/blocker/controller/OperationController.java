package com.example.ibank.blocker.controller;

import com.example.ibank.blocker.api.CheckApi;

import com.example.ibank.blocker.model.CheckRequest;
import com.example.ibank.blocker.service.OperationService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@PreAuthorize( "hasRole('CHECK_API')")
public class OperationController implements CheckApi {

    private final OperationService srv;

    @Override
    public Mono<ResponseEntity<Void>> checkOperation(Mono<CheckRequest> checkRequest, ServerWebExchange exchange) {
        return checkRequest
            .doOnNext( req ->
                log.debug(
                    "checkOperation: login: {}, type: {}, amount: {}",
                    req.getLogin(),
                    req.getOperationType().toString(),
                    req.getAmount().toString()
                )
            )
            .flatMap( req -> srv.checkOperation( req))
            .thenReturn( ResponseEntity.noContent().build())
        ;
    }
}
