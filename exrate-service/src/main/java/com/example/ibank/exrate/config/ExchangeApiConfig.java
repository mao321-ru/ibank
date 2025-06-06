package com.example.ibank.exrate.config;

import com.example.ibank.exrate.exchange.api.SetRateApi;
import com.example.ibank.exrate.exchange.invoker.ApiClient;
import com.example.ibank.exrate.exchange.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class ExchangeApiConfig {

    @Bean
    ApiClient exchangeApiClient( WebClient serviceWebClient) {
        ApiClient apiClient = new ApiClient(
            serviceWebClient.mutate()
                .defaultStatusHandler(
                    status -> status == HttpStatus.CONFLICT,
                    resp ->  resp.bodyToMono( ErrorResponse.class)
                        .flatMap( e ->
                                Mono.error( new IllegalStateException( e.getErrorMessage()))
                        )
                )
                .build()
        );
        apiClient.setBasePath( "exchange");
        return apiClient;
    }

    @Bean
    SetRateApi setRateApi( ApiClient apiClient) {
        return new SetRateApi( apiClient);
    }

}
