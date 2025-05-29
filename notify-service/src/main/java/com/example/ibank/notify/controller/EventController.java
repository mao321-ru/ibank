package com.example.ibank.notify.controller;

import com.example.ibank.notify.api.EventApi;
import com.example.ibank.notify.model.*;

import com.example.ibank.notify.service.EventService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@PreAuthorize( "hasRole('EVENT_API')")
public class EventController implements EventApi {

    private final EventService srv;

    @Override
    public Mono<ResponseEntity<Void>> createEvent(Mono<EventCreate> eventCreate, ServerWebExchange exchange) {
        return eventCreate
            .flatMap( rq -> {
                log.debug( "createEvent: type: {}, login: {}: {}", rq.getEventType(), rq.getUserLogin(), rq.getMessage());
                return srv.createEvent( rq);
            })
            .thenReturn( ResponseEntity.status( HttpStatus.NO_CONTENT).build())
        ;
    }

}
