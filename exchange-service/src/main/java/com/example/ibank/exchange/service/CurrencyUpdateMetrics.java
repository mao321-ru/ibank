package com.example.ibank.exchange.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyUpdateMetrics {
    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicLong> lastUpdateTimes = new ConcurrentHashMap<>();

    public void recordCurrencyUpdate(String currencyCode) {
        AtomicLong lastUpdateTime = lastUpdateTimes.computeIfAbsent(currencyCode, code -> {
            AtomicLong gaugeValue = new AtomicLong(0);
            Gauge.builder("ibank_currency_update_timestamp", gaugeValue::get)
                    .tag( "currencyCode", currencyCode)  // Метка для группировки по валюте
                    .description("Timestamp of last update for currency")
                    .register(meterRegistry);
            return gaugeValue;
        });

        // Обновляем временную метку
        lastUpdateTime.set(Instant.now().getEpochSecond());
    }

}