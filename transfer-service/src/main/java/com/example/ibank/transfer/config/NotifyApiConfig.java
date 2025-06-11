package com.example.ibank.transfer.config;

import com.example.ibank.transfer.notify.api.EventApi;
import com.example.ibank.transfer.notify.invoker.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class NotifyApiConfig {

    @Value( "${notify.url:}")
    private String baseUrl;

    @Bean
    ApiClient notifyApiClient( WebClient serviceWebClient) {
        ApiClient apiClient = new ApiClient( serviceWebClient);
        if( baseUrl != null && !baseUrl.isEmpty()) {
            apiClient.setBasePath( baseUrl);
        }
        return apiClient;
    }

    @Bean
    EventApi eventApi( ApiClient apiClient) {
        return new EventApi( apiClient);
    }
}
