package com.example.ibank.transfer.config;

import com.example.ibank.transfer.notify.api.EventApi;
import com.example.ibank.transfer.notify.invoker.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class NotifyApiConfig {

    @Bean
    ApiClient notifyApiClient( WebClient serviceWebClient) {
        ApiClient apiClient = new ApiClient( serviceWebClient);
        apiClient.setBasePath( "notify");
        return apiClient;
    }

    @Bean
    EventApi eventApi( ApiClient apiClient) {
        return new EventApi( apiClient);
    }
}
