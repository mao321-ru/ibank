package com.example.ibank.front.config;


import com.example.ibank.front.accounts.api.AuthApi;
import com.example.ibank.front.accounts.invoker.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.function.Predicate;

@Configuration
@Slf4j
public class AccountsApiConfig {

    @Bean
    ApiClient apiClient( WebClient authWebClient) {
        ApiClient apiClient = new ApiClient(
            authWebClient.mutate()
                .defaultStatusHandler(
                    status -> status == HttpStatus.CONFLICT,
                    resp ->  Mono.error( new IllegalStateException( "Этот логин уже используется"))
                )
                .build()
        );
        apiClient.setBasePath( "accounts");
        return apiClient;
    }

    @Bean
    AuthApi authApi( ApiClient apiClient) {
        return new AuthApi( apiClient);
    }
}
