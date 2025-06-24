package com.example.ibank.front.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
            .consumeWith( System.out::println) // вывод запроса и ответа
            .jsonPath("$[?(@.currencyCode == 'USD')].rate")
                .value( rates -> {
                    var ra = rates instanceof net.minidev.json.JSONArray ? (net.minidev.json.JSONArray) rates : null;
                    assertThat( ra)
                        .withFailMessage("Unexpected JSON parse class type: " + rates.getClass().getName())
                        .isNotNull();
                    assertThat( ra.size()).withFailMessage("Incorrect rates count for USD").isEqualTo( 1);
                    String rateStr = ra.getFirst().toString();
                    Double rate = Double.valueOf( rateStr);
                    assertThat( rate)
                        .withFailMessage("Initial USD rate received: %s", rateStr)
                        .isNotEqualTo( Double.valueOf( INITIAL_USD_RATE))
                   ;
                })
        ;
    }

}
