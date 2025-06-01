package com.example.ibank.front.service;

import com.example.ibank.front.dto.CashOperationDto;
import reactor.core.publisher.Mono;

public interface CashService {

    Mono<Void> cashOperation(String login, CashOperationDto dto);

}