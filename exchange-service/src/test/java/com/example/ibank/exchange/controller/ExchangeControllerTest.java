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

    @Test
    void getRates_ok() throws Exception {
        wtc.get().uri( "/rates")
            .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .json(
                    """
                    [
                        { "currencyCode": "USD", "currencyName": "Доллар США", "rate": 50},
                        { "currencyCode": "EUR", "currencyName": "Евро", "rate": 55}
                    ]
                    """
            )
        ;
    }

    @Test
    void setRates_ok() throws Exception {
        wtc.post().uri( "/rates")
            .headers( headers -> headers.setBearerAuth( getAccessToken( "exrate-service")))
            .contentType( MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                [
                    { "currencyCode": "USD", "rate": 50 },
                    { "currencyCode": "EUR", "rate": 55 }
                ]
                """
            )
            .exchange()
            .expectStatus().isNoContent()
        ;
    }

}
