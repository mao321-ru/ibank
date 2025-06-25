package com.example.ibank.notify.consumer;

import com.example.ibank.shared.notification.EventCreate;
import com.example.ibank.notify.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyEventsConsumer {

    private final EventService srv;

    @KafkaListener( topics = "${kafka.topic.notify-events}")
    public Mono<Void> consume(ConsumerRecord<String, EventCreate> rec) {
        log.trace( "consume event: {}: {}", rec.key(), rec.value());
        var event = rec.value();
        return
            srv.createEvent( event)
            .doOnSuccess( v -> {
                log.debug( "event saved: {} ", event);
            })
            .doOnError( e -> {
                log.debug( "consume event {}: {} - error: {}", rec.key(), rec.value(), e.getMessage());
            })
        ;
    }

}
