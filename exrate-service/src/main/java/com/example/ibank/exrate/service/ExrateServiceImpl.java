package com.example.ibank.exrate.service;

import com.example.ibank.exrate.exchange.api.SetRateApi;
import com.example.ibank.exrate.exchange.model.RateShort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExrateServiceImpl implements ExrateService {

    private final SetRateApi setRateApi;

    @Scheduled( fixedDelay = 1000)
    @Override
    public Boolean refreshRates() {
        log.debug( "refreshRates");
        return setRateApi.setRates( List.of(
                new RateShort().currencyCode( "USD").rate(
                    BigDecimal.valueOf( ThreadLocalRandom.current().nextDouble(49.00, 51.00))
                        .setScale( 2, RoundingMode.HALF_UP)
                ),
                new RateShort().currencyCode( "EUR").rate(
                    BigDecimal.valueOf( ThreadLocalRandom.current().nextDouble(54.00, 56.00))
                            .setScale( 2, RoundingMode.HALF_UP)
                )
            ))
            .thenReturn( Boolean.TRUE)
            .doOnError( e ->  log.debug( "refreshRate error: {}", e.getMessage()))
            .onErrorReturn( Boolean.FALSE)
            .block()
        ;
    }

}
