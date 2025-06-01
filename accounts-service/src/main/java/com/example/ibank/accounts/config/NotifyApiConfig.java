package com.example.ibank.accounts.config;

import com.example.ibank.accounts.notify.api.EventApi;
import com.example.ibank.accounts.notify.invoker.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class NotifyApiConfig {

    @Bean
    ApiClient notifyApiClient( WebClient authWebClient) {
        ApiClient apiClient = new ApiClient( authWebClient);
        apiClient.setBasePath( "notify");
        return apiClient;
    }

    @Bean
    EventApi eventApi( ApiClient apiClient) {
        return new EventApi( apiClient);
    }
}
