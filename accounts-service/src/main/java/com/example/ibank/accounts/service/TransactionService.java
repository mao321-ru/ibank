package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.CashTransactionRequest;
import reactor.core.publisher.Mono;

public interface TransactionService {

    Mono<Void> createCashTransaction(CashTransactionRequest rq);

}
