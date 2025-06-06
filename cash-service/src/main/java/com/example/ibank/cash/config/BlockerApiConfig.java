package com.example.ibank.cash.config;

import com.example.ibank.cash.blocker.model.ErrorResponse;
import com.example.ibank.cash.blocker.api.CheckApi;
import com.example.ibank.cash.blocker.invoker.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class BlockerApiConfig {

    @Bean
    ApiClient blockerApiClient( WebClient serviceWebClient) {
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
        apiClient.setBasePath( "blocker");
        return apiClient;
    }

    @Bean
    CheckApi checkApi( ApiClient apiClient) {
        return new CheckApi( apiClient);
    }
}
