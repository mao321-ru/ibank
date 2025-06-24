package com.example.ibank.exchange.consumer;

import com.example.ibank.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrentRatesConsumer {

    private final ExchangeService srv;

    @KafkaListener( topics = "${kafka.topic.current-rates}")
    public Mono<Void> consume(ConsumerRecord<String, String> rec) {
        log.debug( "rate {}: {}", rec.key(), rec.value());
        return
            srv.setRates(
                List.of( new ExchangeService.RateShort( rec.key(), new BigDecimal( rec.value())))
            )
            .doOnSuccess( v -> {
                log.debug( "consume rate {}: {} - finished OK", rec.key(), rec.value());
            })
            .doOnError( e -> {
                log.debug( "consume rate {}: {} - error: {}", rec.key(), rec.value(), e.getMessage());
            })
        ;
    }

}
