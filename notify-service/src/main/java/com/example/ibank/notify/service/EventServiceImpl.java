package com.example.ibank.notify.service;

import com.example.ibank.notify.model.*;
import com.example.ibank.notify.repository.EventRepository;

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

    @Override
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
            .then();
    }

}
