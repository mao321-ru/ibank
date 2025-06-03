package com.example.ibank.exchange.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ExchangeControllerTest extends ControllerTest {

    @Test
    void exchange_ok() throws Exception {
        wtc.post().uri( "/exchange")
            .headers( headers -> headers.setBearerAuth( getAccessToken( "transfer-service")))
            .contentType( MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "amount": 150,
                    "currency": "RUB",
                    "toCurrency": "USD"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .json(
                """
                {
                    "amount": 3,
                    "currency": "USD"
                }
                """
            )
        ;
    }

}
