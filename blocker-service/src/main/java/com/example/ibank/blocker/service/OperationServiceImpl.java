package com.example.ibank.blocker.service;

import com.example.ibank.blocker.model.CheckRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    @Value( "${operation.cash.limit}")
    private BigDecimal cashLimit;

    @Value( "${operation.transfer.limit}")
    private BigDecimal transferLimit;

    private final MeterRegistry meterRegistry;

    @Override
    public Mono<Void> checkOperation(CheckRequest req) {
        BigDecimal limitAmount = switch( req.getOperationType()) {
            case DEPOSIT, WITHDRAWAL -> cashLimit;
            case TRANSFER -> transferLimit;
            default -> null;
        };
        return limitAmount != null && req.getAmount().compareTo( limitAmount) > 0
            ? Mono.error( new IllegalStateException(
                    "Сумма по операции не должна превышать %s %s".formatted( limitAmount.toString(), req.getCurrency())
                ))
                .doOnError( e ->
                    Counter.builder( "ibank_operation_block")
                        .tag( "login", req.getLogin())
                        .tag( "currency", req.getCurrency())
                        .tag( "to_login", req.getToLogin())
                        .tag( "to_currency", req.getToCurrency())
                        .register( meterRegistry)
                        .increment()
                )
                .cast( Void.class)
            : Mono.empty()
        ;
    }
}
