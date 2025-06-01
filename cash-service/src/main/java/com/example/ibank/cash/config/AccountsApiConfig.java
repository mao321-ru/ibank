package com.example.ibank.cash.config;


import com.example.ibank.cash.accounts.api.TrCashApi;
import com.example.ibank.cash.accounts.invoker.ApiClient;
import com.example.ibank.cash.accounts.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class AccountsApiConfig {

    @Bean
    ApiClient accountsApiClient( WebClient authWebClient) {
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
        apiClient.setBasePath( "accounts");
        return apiClient;
    }

    @Bean
    TrCashApi trCashApi(ApiClient apiClient) {
        return new TrCashApi( apiClient);
    }
}
