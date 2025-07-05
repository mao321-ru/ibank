package com.example.ibank.notify.config;

import com.example.ibank.shared.notification.EventCreate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;

import java.util.Collection;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventCreate> kafkaListenerContainerFactory(
        ConsumerFactory<String, EventCreate> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, EventCreate> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory( consumerFactory);
        // включаем использование контекста трассировки из заголовка traceparent сообщения Kafka
        factory.getContainerProperties().setObservationEnabled( true);
        return factory;
    }

}
