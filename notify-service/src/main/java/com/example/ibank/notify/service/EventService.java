package com.example.ibank.notify.service;

import com.example.ibank.notify.model.*;
import reactor.core.publisher.Mono;

public interface EventService {

    Mono<Void> createEvent( EventCreate rq);

}
