package com.example.ibank.blocker.service;

import com.example.ibank.blocker.model.*;
import reactor.core.publisher.Mono;

public interface OperationService {

    Mono<Void> checkOperation(CheckRequest req);
}
