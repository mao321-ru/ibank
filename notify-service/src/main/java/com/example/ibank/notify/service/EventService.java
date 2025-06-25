package com.example.ibank.notify.service;

import com.example.ibank.shared.notification.EventCreate;
import reactor.core.publisher.Mono;

public interface EventService {

    Mono<Void> createEvent( EventCreate rq);

}
