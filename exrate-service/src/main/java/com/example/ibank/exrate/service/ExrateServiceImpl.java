package com.example.ibank.exrate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExrateServiceImpl implements ExrateService {

    @Value( "${kafka.topic.current-rates}")
    String currentRatesTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private volatile Boolean lastRefreshOk = null;

    @Override
    public Boolean isLastRefreshOk() {
        return lastRefreshOk;
    }

    // fixedDelay по факту не обеспечивает запуска следующей задачи только по завершении предыдущей
    // (даже интервал в 1 сек между запусками нестабильно обеспечивает)
    @Scheduled( fixedDelay = 1000)
    @Override
    public void refreshRates() {
        var newUsd = BigDecimal.valueOf( ThreadLocalRandom.current().nextDouble(49.00, 51.00))
                .setScale( 2, RoundingMode.HALF_UP);
        var newEuro = BigDecimal.valueOf( ThreadLocalRandom.current().nextDouble(54.00, 56.00))
                .setScale( 2, RoundingMode.HALF_UP);
        String usdRate = "USD " + newUsd;
        log.debug( "refreshRates: {}", usdRate);

        try {
            kafkaTemplate.send( currentRatesTopic, "USD", newUsd.toString());
            kafkaTemplate.send( currentRatesTopic, "EUR", newEuro.toString());
            lastRefreshOk = Boolean.TRUE;
            log.debug( "refreshRates: {}: finished OK", usdRate);
        }
        catch ( Throwable e) {
            lastRefreshOk = Boolean.FALSE;
            log.debug( "refreshRate: {}: error: {}", usdRate, e.getMessage());
            throw e;
        }
    }

}
