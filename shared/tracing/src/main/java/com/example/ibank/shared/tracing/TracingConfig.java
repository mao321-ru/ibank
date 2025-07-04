package com.example.ibank.shared.tracing;

import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import jakarta.annotation.PostConstruct;

@Configuration
public class TracingConfig {

    @PostConstruct
    public void setup() {
        // Регистрируем хук для автоматической привязки контекста
        Hooks.enableAutomaticContextPropagation();
    }

}