package com.example.ibank.transfer.service;

import com.example.ibank.transfer.model.TransferRequest;
import reactor.core.publisher.Mono;

public interface TransferService {

    Mono<Void> transfer(TransferRequest req);
}
