package com.example.ibank.blocker.service;

import com.example.ibank.blocker.model.CheckRequest;
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
            : Mono.empty()
        ;
    }
}
