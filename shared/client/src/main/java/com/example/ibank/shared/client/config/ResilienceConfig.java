package com.example.ibank.shared.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;


@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger( ResilienceConfig.class);

    @Bean
    public WebClient.Builder resilientWebClientBuilder(
        ReactiveCircuitBreakerFactory cbFactory,
        @Qualifier("authWebClientBuilder") WebClient.Builder builder
    ) {
        return builder
            .filter( circuitBreakerFilter(cbFactory))
            .filter( retryFilter())
        ;
    }

    private ExchangeFilterFunction circuitBreakerFilter(
        ReactiveCircuitBreakerFactory cbFactory
    ) {
        return (request, next) ->
           // используем дефолнтные настройки, но можно было выделить указать имя сервиса вместо global-cb
            cbFactory.create( "global-cb")
            .run(
                next.exchange( request),
                throwable -> {
                    log.error( "CircuitBreaker triggered: {}", throwable.getMessage());
                    return Mono.just( ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE)
                        .header("X-CircuitBreaker-Enabled", "true")
                        .header("Content-Type", "application/json")
                        .body(
                            """
                            {
                                "status": "FALLBACK",
                                "message": "Service temporary unavailable (from local circuitBreakerFilter)"
                            }
                            """)
                        .build()
                    );
                }
            );
    }

    private ExchangeFilterFunction retryFilter() {
        return (request, next) -> next.exchange(request)
            .retryWhen(
                Retry.backoff(3, Duration.ofMillis(100))
                .filter( this::shouldRetry)
            );
    }

    private boolean shouldRetry(Throwable ex) {
        return ex instanceof WebClientResponseException wcre
                ? wcre.getStatusCode().is5xxServerError()
                : ex instanceof TimeoutException;
    }

}