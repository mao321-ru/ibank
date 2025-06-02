package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.CashTransactionRequest;
import com.example.ibank.accounts.model.TransferTransactionRequest;
import reactor.core.publisher.Mono;

public interface TransactionService {

    Mono<Void> createCashTransaction(CashTransactionRequest rq);

    Mono<Void> createTransferTransaction(TransferTransactionRequest rq);
}
