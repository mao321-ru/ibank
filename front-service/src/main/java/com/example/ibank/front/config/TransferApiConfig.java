package com.example.ibank.front.config;

import com.example.ibank.front.transfer.api.TransferApi;
import com.example.ibank.front.transfer.invoker.ApiClient;
import com.example.ibank.front.transfer.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class TransferApiConfig {

    @Value( "${transfer.url:}")
    private String baseUrl;

    @Bean
    ApiClient transferApiClient( WebClient serviceWebClient) {
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
    TransferApi transferApi( ApiClient apiClient) {
        return new TransferApi( apiClient);
    }
}