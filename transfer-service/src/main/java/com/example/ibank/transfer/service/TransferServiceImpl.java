package com.example.ibank.transfer.service;

import com.example.ibank.transfer.accounts.api.TrTransferApi;
import com.example.ibank.transfer.accounts.model.TransferTransactionRequest;
import com.example.ibank.transfer.blocker.api.CheckApi;
import com.example.ibank.transfer.blocker.model.CheckRequest;
import com.example.ibank.transfer.model.*;
import com.example.ibank.transfer.notify.api.EventApi;
import com.example.ibank.transfer.notify.model.EventCreate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final CheckApi checkApi;
    private final TrTransferApi trTransferApi;
    private final EventApi eventApi;

    @Override
    public Mono<Void> transfer( TransferRequest req) {
        BigDecimal toAmount = req.getAmount();
        if( ! req.getCurrency().equals( req.getToCurrency())) {
            throw new IllegalStateException( "Exchange not implemented");
        }
        return
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
            .then(
                eventApi.createEvent( new EventCreate()
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
                )
                .doOnError( e -> log.error( "Notification failed: {}", e.getMessage()))
                .onErrorResume( e -> Mono.empty())
                .then()
            )
        ;
    }
}
