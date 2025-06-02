package com.example.ibank.transfer.config;

import com.example.ibank.transfer.blocker.model.ErrorResponse;
import com.example.ibank.transfer.blocker.api.CheckApi;
import com.example.ibank.transfer.blocker.invoker.ApiClient;
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
    ApiClient blockerApiClient( WebClient authWebClient) {
        ApiClient apiClient = new ApiClient(
            authWebClient.mutate()
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
