package com.example.ibank.exrate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> producerFactory
    ) {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>( producerFactory);
        // обеспечиваем передачу traceparent в заголовке сообщения kafka
        kafkaTemplate.setObservationEnabled( true);
        return kafkaTemplate;
    }
}
