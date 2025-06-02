package com.example.ibank.front.service;

import com.example.ibank.front.dto.CashOperationDto;
import com.example.ibank.front.dto.TransferDto;
import reactor.core.publisher.Mono;

public interface MoneyService {

    Mono<Void> cashOperation(String login, CashOperationDto dto);

    Mono<Void> transfer(String login, TransferDto dto);
}