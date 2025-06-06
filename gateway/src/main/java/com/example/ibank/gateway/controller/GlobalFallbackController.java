package com.example.ibank.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

@RestController
public class GlobalFallbackController {

    private static final Logger log = LoggerFactory.getLogger( GlobalFallbackController.class);


    // Глобальный пользовательский fallback (для примера)
    @RequestMapping(
        path = "/default-fallback",
        method = {RequestMethod.GET, RequestMethod.POST}
    )
    public Mono<ResponseEntity<Map<String, Object>>> defaultFallback(
        ServerHttpRequest request,
        ServerWebExchange exchange
    ) {
        // Получаем оригинальный путь из атрибутов Gateway
        Set<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Collections.emptySet());
        String originalPath = (uris.isEmpty()) ? "/unknown-path" : URI.create( uris.iterator().next().toString()).getPath();
        log.warn("Fallback triggered for path: {}", originalPath);
        return Mono.just( ResponseEntity
            .status( HttpStatus.SERVICE_UNAVAILABLE)
            .body( Map.of(
                "timestamp", Instant.now(),
                "status", 503,
                "error", "Service Unavailable",
                "path", originalPath,
                "message", "gateway: Service unavailable"
            ))
        );
    }
}
