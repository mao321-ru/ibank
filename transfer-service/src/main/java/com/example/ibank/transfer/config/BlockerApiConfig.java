package com.example.ibank.transfer.config;

import com.example.ibank.transfer.blocker.model.ErrorResponse;
import com.example.ibank.transfer.blocker.api.CheckApi;
import com.example.ibank.transfer.blocker.invoker.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class BlockerApiConfig {

    @Value( "${blocker.url:}")
    private String baseUrl;

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
        if( baseUrl != null && !baseUrl.isEmpty()) {
            apiClient.setBasePath( baseUrl);
        }
        return apiClient;
    }

    @Bean
    CheckApi checkApi( ApiClient apiClient) {
        return new CheckApi( apiClient);
    }
}
