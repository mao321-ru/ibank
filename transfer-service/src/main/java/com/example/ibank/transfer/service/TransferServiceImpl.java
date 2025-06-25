package com.example.ibank.transfer.service;

import com.example.ibank.shared.notification.EventCreate;
import com.example.ibank.shared.notification.EventApi;
import com.example.ibank.transfer.accounts.api.TrTransferApi;
import com.example.ibank.transfer.accounts.model.TransferTransactionRequest;
import com.example.ibank.transfer.blocker.api.CheckApi;
import com.example.ibank.transfer.blocker.model.CheckRequest;
import com.example.ibank.transfer.exchange.api.ExchangeApi;
import com.example.ibank.transfer.exchange.model.ExchangeRequest;
import com.example.ibank.transfer.exchange.model.ExchangeResponse;
import com.example.ibank.transfer.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final ExchangeApi exchangeApi;
    private final CheckApi checkApi;
    private final TrTransferApi trTransferApi;
    private final EventApi eventApi;

    @Override
    public Mono<Void> transfer( TransferRequest req) {
        return
            ( req.getCurrency().equals( req.getToCurrency())
                ? Mono.just( req.getAmount())
                : exchangeApi.exchange( new ExchangeRequest()
                        .amount( req.getAmount())
                        .currency( req.getCurrency())
                        .toCurrency( req.getToCurrency())
                    )
                    .map( ExchangeResponse::getAmount)
            )
            .flatMap( toAmount ->
                checkApi.checkOperation( new CheckRequest()
                    .login( req.getLogin())
                    .operationType( CheckRequest.OperationTypeEnum.TRANSFER)
                    .amount( req.getAmount())
                    .currency( req.getCurrency())
                )
                .then(
                    trTransferApi.createTransferTransaction( new TransferTransactionRequest()
                        .login( req.getLogin())
                        .amount( req.getAmount())
                        .currency( req.getCurrency())
                        .toLogin( req.getToLogin())
                        .toAmount( toAmount)
                        .toCurrency( req.getToCurrency())
                    )
                )
            )
            .then(
                eventApi.createEvent( EventCreate.builder()
                    .source( "transfer-service")
                    .eventType( "transfer")
                    .userLogin( req.getLogin())
                    .message(
                        "Перевод в сумме " + req.getAmount().toString() + " " + req.getCurrency()
                        + ( req.getLogin().equals( req.getToLogin())
                                ? ""
                                : " пользователю [%s]".formatted( req.getToLogin())
                        )
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
