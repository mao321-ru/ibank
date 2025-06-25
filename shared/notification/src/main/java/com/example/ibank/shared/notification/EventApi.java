package com.example.ibank.shared.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class EventApi {

    @Value( "${kafka.topic.notify-events}")
    String notifyEventsTopic;

    @Autowired
    private KafkaTemplate<String, EventCreate> kafkaTemplate;

    public Mono<Void> createEvent( EventCreate event)  {
        return Mono.fromRunnable( () ->
            kafkaTemplate.send( notifyEventsTopic, event.getSource(), event)
        );
    }

}
