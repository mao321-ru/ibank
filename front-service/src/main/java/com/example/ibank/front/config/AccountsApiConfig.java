package com.example.ibank.front.config;


import com.example.ibank.front.accounts.api.UserApi;
import com.example.ibank.front.accounts.invoker.ApiClient;
import com.example.ibank.front.accounts.model.Error;
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
                    resp ->  resp.bodyToMono( Error.class)
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
    UserApi usersApi( ApiClient apiClient) {
        return new UserApi( apiClient);
    }
}
