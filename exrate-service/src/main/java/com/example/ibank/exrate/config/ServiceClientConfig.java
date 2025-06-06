package com.example.ibank.exrate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class ServiceClientConfig {

    // Включено логирование http-запросов к сервисам
    @Value( "${logging.service.http:false}")
    private Boolean isLoggingHttp;

    @Bean( "serviceWebClient")
    WebClient serviceWebClient(
        @Qualifier( "resilientWebClientBuilder") WebClient.Builder builder
    ) {
        return isLoggingHttp
            ? builder.filter( loggingFilter()).build()
            : builder.build()
        ;
    }

    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            // Логирование запроса
            StringBuilder requestLog = new StringBuilder();
            requestLog.append("\n=== REQUEST ===\n");
            requestLog.append(request.method()).append(" ").append(request.url()).append("\n");

            // Заголовки запроса
            requestLog.append("Headers:\n");
            request.headers().forEach((name, values) ->
                    requestLog.append("  ").append(name).append(": ").append(String.join(", ", values)).append("\n"));

            // Тело запроса не логируется

            log.debug(requestLog.toString());
            return Mono.just( ClientRequest.from( request).build());
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(response -> {
            // Логирование ответа
            return response.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> {
                        StringBuilder responseLog = new StringBuilder();
                        responseLog.append("\n=== RESPONSE ===\n");
                        responseLog.append("Status: ").append(response.statusCode()).append("\n");

                        // Заголовки ответа
                        responseLog.append("Headers:\n");
                        response.headers().asHttpHeaders().forEach((name, values) ->
                                responseLog.append("  ").append(name).append(": ").append(String.join(", ", values)).append("\n"));

                        // Тело ответа
                        responseLog.append("Body:\n").append(body);

                        log.debug(responseLog.toString());

                        // Восстанавливаем оригинальный response с телом
                        return Mono.just(ClientResponse.from(response)
                                .body(body)
                                .build());
                    });
        }));
    }

}