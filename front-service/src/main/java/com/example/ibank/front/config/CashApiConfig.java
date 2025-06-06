package com.example.ibank.front.config;

import com.example.ibank.front.cash.api.CashApi;
import com.example.ibank.front.cash.invoker.ApiClient;
import com.example.ibank.front.cash.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class CashApiConfig {

    @Bean
    ApiClient cashApiClient( WebClient serviceWebClient) {
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
        apiClient.setBasePath( "cash");
        return apiClient;
    }

    @Bean
    CashApi cashApi( ApiClient apiClient) {
        return new CashApi( apiClient);
    }
}