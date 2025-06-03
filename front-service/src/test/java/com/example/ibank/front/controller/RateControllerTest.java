package com.example.ibank.front.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class RateControllerTest extends ControllerTest {


    @Test
    void rates_ok() throws Exception {
        final String login = EXISTS_USER_LOGIN;
        final String password = EXISTS_USER_PASSWORD;

        String sessionCookie = checkLoginOk( login, password);

        wtc.get().uri( "/rates")
            .cookie("SESSION", sessionCookie)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType( MediaType.APPLICATION_JSON)
            .expectBody()
            //.consumeWith( System.out::println) // вывод запроса и ответа
            .json(
                    """
                    [
                        { "currencyCode": "USD", "currencyName": "Доллар США", "rate": 50.000000},
                        { "currencyCode": "EUR", "currencyName": "Евро", "rate": 55.000000}
                    ]
                    """
            )
        ;
    }

}
