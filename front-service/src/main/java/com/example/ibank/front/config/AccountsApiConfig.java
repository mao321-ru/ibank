package com.example.ibank.front.config;


import com.example.ibank.front.accounts.api.AuthApi;
import com.example.ibank.front.accounts.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AccountsApiConfig {

    @Bean
    ApiClient apiClient( WebClient webClient) {
        ApiClient apiClient = new ApiClient( webClient);
        apiClient.setBasePath( "accounts");
        return apiClient;
    }

    @Bean
    AuthApi authApi( ApiClient apiClient) {
        return new AuthApi( apiClient);
    }
}
