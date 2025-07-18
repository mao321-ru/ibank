package com.example.ibank.cash.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@Slf4j
public class CashControllerTest extends ControllerTest {

    @Test
    void deposit_ok() throws Exception {
        wtc.post().uri( "/cash/deposit")
                .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                    """
                    {
                        "login": "%s",
                        "amount": "100.05",
                        "currency": "RUB"
                    }
                    """.formatted(CASH_USER_LOGIN)
                )
                .exchange()
                .expectStatus().isNoContent()
                //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;
        log.info( "waiting 0.5 seconds after send event ...");
        Thread.sleep( 500);
    }

}
