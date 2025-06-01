package com.example.ibank.cash.controller;

import com.example.ibank.cash.api.CashApi;
import com.example.ibank.cash.model.*;

import com.example.ibank.cash.service.CashService;

import com.example.ibank.cash.service.enums.CashOperation;
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
@PreAuthorize( "hasRole('CASH_API')")
public class CashController implements CashApi {

    private final CashService srv;

    @Override
    public Mono<ResponseEntity<Void>> deposit(Mono<CashOperationRequest> cashOperationRequest, ServerWebExchange exchange) {
        return cashOperationRequest
            .flatMap( req -> srv.processOperation( CashOperation.DEPOSIT, req))
            .thenReturn( ResponseEntity.noContent().build())
        ;
    }

    @Override
    public Mono<ResponseEntity<Void>> withdraw(Mono<CashOperationRequest> cashOperationRequest, ServerWebExchange exchange) {
        return cashOperationRequest
            .flatMap( req -> srv.processOperation( CashOperation.WITHDRAW, req))
            .thenReturn( ResponseEntity.noContent().build())
        ;
    }
}
