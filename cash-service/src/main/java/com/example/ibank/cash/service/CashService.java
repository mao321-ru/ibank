package com.example.ibank.cash.service;

import com.example.ibank.cash.model.*;
import com.example.ibank.cash.service.enums.CashOperation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CashService {

    Mono<Void> processOperation(CashOperation cashOperation, CashOperationRequest req);
}
