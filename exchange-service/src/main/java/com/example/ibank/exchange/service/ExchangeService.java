package com.example.ibank.exchange.service;

import com.example.ibank.exchange.model.CurrentRate;
import com.example.ibank.exchange.model.ExchangeRequest;
import com.example.ibank.exchange.model.ExchangeResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExchangeService {
    Mono<ExchangeResponse> exchange(ExchangeRequest rq);

    Flux<CurrentRate> getRates();

}
