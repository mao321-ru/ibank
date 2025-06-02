package com.example.ibank.front.service;

import com.example.ibank.front.cash.api.CashApi;
import com.example.ibank.front.cash.model.CashOperationRequest;
import com.example.ibank.front.controller.enums.CashAction;
import com.example.ibank.front.dto.CashOperationDto;
import com.example.ibank.front.dto.TransferDto;
import com.example.ibank.front.transfer.api.TransferApi;
import com.example.ibank.front.transfer.model.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoneyServiceImpl implements MoneyService {

    private final CashApi cashApi;
    private final TransferApi transferApi;

    @Override
    public Mono<Void> cashOperation(String login, CashOperationDto dto) {
        var rq = new CashOperationRequest()
            .login( login)
            .currency( dto.getCurrency())
            .amount( dto.getAmount())
        ;
        return
            switch ( dto.getAction()) {
                case CashAction.PUT -> cashApi.deposit( rq);
                case CashAction.GET -> cashApi.withdraw( rq);
            }
        ;
    }

    @Override
    public Mono<Void> transfer(String login, TransferDto dto) {
        return
            transferApi.transfer( new TransferRequest()
                .login( login)
                .amount( dto.getAmount())
                .currency( dto.getFromCurrency())
                .toLogin( dto.getToLogin())
                .toCurrency( dto.getToCurrency())
            );
    }
}