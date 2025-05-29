package com.example.ibank.notify.repository;

import com.example.ibank.notify.model.Event;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface EventRepository extends R2dbcRepository<Event, Long> {

}
