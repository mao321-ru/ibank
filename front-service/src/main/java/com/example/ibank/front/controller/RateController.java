package com.example.ibank.front.controller;

import com.example.ibank.front.exchange.api.RateApi;
import com.example.ibank.front.exchange.model.CurrentRate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class RateController {

    private final RateApi rateApi;

    @GetMapping( "/rates")
    Mono<ResponseEntity<Flux<CurrentRate>>> rates() {
        log.trace( "rates:");
        return Mono.just( ResponseEntity.ok( rateApi.getRates() ));
    }

}
