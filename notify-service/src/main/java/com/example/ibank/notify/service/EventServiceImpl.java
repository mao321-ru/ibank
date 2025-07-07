package com.example.ibank.notify.service;

import com.example.ibank.notify.model.*;
import com.example.ibank.shared.notification.EventCreate;
import com.example.ibank.notify.repository.EventRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository repo;

    private final MeterRegistry meterRegistry;

    @Override
    @NewSpan( "db")
    @Transactional
    public Mono<Void> createEvent( EventCreate rq) {
        return repo
            .save( Event.builder()
                .source( rq.getSource())
                .eventType( rq.getEventType())
                .eventTime( rq.getEventTime())
                .userLogin( rq.getUserLogin())
                .message( rq.getMessage())
                .build()
            )
            .doOnNext( e -> log.trace( "created event_id: {}", e.getId()))
            .doOnError( e ->
                Counter.builder( "ibank_notify_error")
                    .tag( "login", rq.getUserLogin())
                    .register( meterRegistry)
                    .increment()
            )
            .then();
    }

}
