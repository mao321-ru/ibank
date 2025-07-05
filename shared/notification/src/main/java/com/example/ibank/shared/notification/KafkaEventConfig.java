package com.example.ibank.shared.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaEventConfig {

    @Bean
    public KafkaTemplate<String, EventCreate> kafkaTemplate(
            ProducerFactory<String, EventCreate> producerFactory
    ) {
        KafkaTemplate<String, EventCreate> kafkaTemplate = new KafkaTemplate<>( producerFactory);
        // обеспечиваем передачу traceparent в заголовке сообщения kafka
        kafkaTemplate.setObservationEnabled( true);
        return kafkaTemplate;
    }
}
