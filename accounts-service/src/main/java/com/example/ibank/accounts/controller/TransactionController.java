package com.example.ibank.accounts.controller;

import com.example.ibank.accounts.api.TrCashApi;
import com.example.ibank.accounts.api.UserApi;
import com.example.ibank.accounts.model.*;
import com.example.ibank.accounts.service.TransactionService;
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
public class TransactionController implements TrCashApi {

    private final TransactionService srv;

    @PreAuthorize( "hasRole('CASH')")
    @Override
    public Mono<ResponseEntity<Void>> createCashTransaction(Mono<CashTransactionRequest> cashTransactionRequest, ServerWebExchange exchange) {
        return cashTransactionRequest
                .flatMap( rq -> {
                    log.debug( "createCashTransaction: login: {}, amount: {} {}", rq.getLogin(), rq.getAmount().toString(), rq.getCurrency());
                    return srv.createCashTransaction( rq);
                })
                .thenReturn( ResponseEntity.noContent().build())
        ;
    }
}
