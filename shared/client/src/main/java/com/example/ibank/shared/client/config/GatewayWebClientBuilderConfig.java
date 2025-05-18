package com.example.ibank.shared.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Configuration
@Slf4j
public class GatewayWebClientBuilderConfig {

    @Value( "${gateway.url:}")
    private String gatewayUrl;

    @Bean
    public WebClient.Builder gatewayWebClientBuilder( DiscoveryClient discoveryClient) {
        return WebClient.builder()
            .filter( ( req, nextFilter) -> {
                URI newUrl = URI.create( getGatewayUrl( discoveryClient) + "/api/" + req.url());
                var newReq = ClientRequest.from( req).url( newUrl).build();
                return nextFilter.exchange( newReq);
            });
    }

    private String getGatewayUrl( DiscoveryClient discoveryClient) {
        return Optional.ofNullable( gatewayUrl)
            .filter( s -> ! s.isEmpty())
            .orElseGet( () -> {
                // используем первый инстантс gateway из eureka на момент выполнения запроса
                List<ServiceInstance> instances = discoveryClient.getInstances( "gateway");
                if ( instances.isEmpty()) {
                    throw new RuntimeException( "Gateway not available");
                }
                ServiceInstance gateway = instances.getFirst();
                return "http://%s:%d".formatted(gateway.getHost(), gateway.getPort());
            });
    }

}