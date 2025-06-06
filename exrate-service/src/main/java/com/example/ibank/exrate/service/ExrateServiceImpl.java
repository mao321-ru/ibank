package com.example.ibank.exrate.service;

import com.example.ibank.exrate.exchange.api.SetRateApi;
import com.example.ibank.exrate.exchange.model.RateShort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExrateServiceImpl implements ExrateService {

    private final SetRateApi setRateApi;

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
        String usdRate = "USD " + newUsd;
        log.debug( "refreshRates: {}", usdRate);
        setRateApi.setRates( List.of(
                new RateShort().currencyCode( "USD").rate( newUsd),
                new RateShort().currencyCode( "EUR").rate(
                    BigDecimal.valueOf( ThreadLocalRandom.current().nextDouble(54.00, 56.00))
                            .setScale( 2, RoundingMode.HALF_UP)
                )
            ))
            .doOnSuccess( v -> {
                lastRefreshOk = Boolean.TRUE;
                log.debug( "refreshRates: {}: finished OK", usdRate);
            })
            .doOnError( e -> {
                lastRefreshOk = Boolean.FALSE;
                log.debug( "refreshRate: {}: error: {}", usdRate, e.getMessage());
            })
            .onErrorComplete()
            // использовал subscribeOn/subscribe вместо .block() чтобы сохранить асинхронность
            // использовал single() вместо текущего immeidate() чтобы не возникала ситуация запуска второго потока
            // до завершения первого, но не помогло - все равно проблема возникает (и второй заврешатся с ошибкой
            // из-за нарушения уникальности БД)
            .subscribeOn( Schedulers.single())
            //.subscribeOn( Schedulers.immediate())
            .subscribe()
        ;
    }

}
