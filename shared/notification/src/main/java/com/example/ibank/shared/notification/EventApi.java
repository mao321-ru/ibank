package com.example.ibank.shared.notification;

import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventApi {

    @Value( "${kafka.topic.notify-events}")
    String notifyEventsTopic;

    private final KafkaTemplate<String, EventCreate> kafkaTemplate;

    @NewSpan( "kafka-send")
    public Mono<Void> createEvent( EventCreate event)  {
        return Mono.fromFuture(
                kafkaTemplate.send( notifyEventsTopic, event.getSource(), event)
            )
            .doOnError( e -> {
                log.error( "error on send event {}: {}", event, e.getMessage());
            })
            .then();
    }

}
