package com.example.ibank.cash.service;

import com.example.ibank.shared.notification.EventCreate;
import com.example.ibank.shared.notification.EventApi;
import com.example.ibank.cash.accounts.api.TrCashApi;
import com.example.ibank.cash.accounts.model.CashTransactionRequest;
import com.example.ibank.cash.blocker.api.CheckApi;
import com.example.ibank.cash.blocker.model.CheckRequest;
import com.example.ibank.cash.model.*;

import com.example.ibank.cash.service.enums.CashOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {

    private final CheckApi checkApi;
    private final TrCashApi trCashApi;
    private final EventApi eventApi;

    @Override
    public Mono<Void> processOperation( CashOperation cashOperation, CashOperationRequest req) {
        return
            checkApi.checkOperation( new CheckRequest()
                .login( req.getLogin())
                .operationType(
                    switch ( cashOperation) {
                        case DEPOSIT -> CheckRequest.OperationTypeEnum.DEPOSIT;
                        case WITHDRAW -> CheckRequest.OperationTypeEnum.WITHDRAWAL;
                    }
                )
                .amount( req.getAmount())
                .currency( req.getCurrency())
            )
            .then(
                trCashApi.createCashTransaction( new CashTransactionRequest()
                    .login( req.getLogin())
                    .amount(
                        switch ( cashOperation) {
                            case DEPOSIT -> req.getAmount();
                            case WITHDRAW -> req.getAmount().negate();
                        }
                    )
                    .currency( req.getCurrency())
                )
            )
            .then(
                eventApi.createEvent( EventCreate.builder()
                    .source( "cash-service")
                    .eventType( cashOperation.toString().toLowerCase())
                    .userLogin( req.getLogin())
                    .message(
                        switch ( cashOperation) {
                            case DEPOSIT -> "Пополнение счета в сумме ";
                            case WITHDRAW -> "Снятие со счета суммы ";
                        }
                        + req.getAmount().toString() + " " + req.getCurrency()
                    )
                    .build()
                )
                .doOnError( e -> log.error( "Notification failed: {}", e.getMessage()))
                .onErrorResume( e -> Mono.empty())
                .then()
            )
        ;
    }
}
