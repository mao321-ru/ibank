package com.example.ibank.shared.notification;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
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

    private final Tracer tracer;
    private final ContextSnapshotFactory contextSnapshotFactory = ContextSnapshotFactory.builder().build();

    private final KafkaTemplate<String, EventCreate> kafkaTemplate;

    public Mono<Void> createEvent( EventCreate event)  {
        return Mono.deferContextual( ctx -> {
            try (ContextSnapshot.Scope scope = contextSnapshotFactory
                        .setThreadLocalsFrom( ctx, ObservationThreadLocalAccessor.KEY)) {
                Span span = tracer.nextSpan().name("kafka-send-event").start();
                return Mono.fromFuture(
                        kafkaTemplate.send( notifyEventsTopic, event.getSource(), event)
                    )
                    .doOnError( e -> {
                        log.error( "error on send event {}: {}", event, e.getMessage());
                        span.error(e);
                    })
                    .doFinally( signal -> {
                        span.end();
                    })
                    .then();
            }
        });
    }

}