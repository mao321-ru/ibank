package com.example.ibank.exchange.controller;

import com.example.ibank.exchange.api.ExchangeApi;
import com.example.ibank.exchange.api.RateApi;
import com.example.ibank.exchange.model.CurrentRate;
import com.example.ibank.exchange.model.ExchangeRequest;
import com.example.ibank.exchange.model.ExchangeResponse;
import com.example.ibank.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExchangeController implements ExchangeApi, RateApi {

    private final ExchangeService srv;

    @PreAuthorize( "hasRole('EXCHANGE_API')")
    @Override
    public Mono<ResponseEntity<ExchangeResponse>> exchange(Mono<ExchangeRequest> exchangeRequest, ServerWebExchange exchange) {
        return exchangeRequest
            .flatMap( rq -> {
                log.debug( "exchange: {} {} -> {}", rq.getAmount(), rq.getCurrency(), rq.getToCurrency());
                return srv.exchange( rq);
            })
            .map( ResponseEntity::ok)
        ;
    }

    @PreAuthorize( "hasRole('RATE_API')")
    @Override
    public Mono<ResponseEntity<Flux<CurrentRate>>> getRates(ServerWebExchange exchange) {
        log.trace( "getRates: ...");
        return Mono.just( ResponseEntity.ok( srv.getRates()));
    }

}
