package com.example.ibank.accounts.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

public class AuthControllerTest extends ControllerTest {

    @Test
    void validate_ok() throws Exception {
        wtc.post().uri( "/auth/validate")
            .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
            .contentType( MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "login": "%s",
                    "password": "%s"
                }
                """.formatted( EXISTS_USER_LOGIN, EXISTS_USER_PASSWORD)
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            //.consumeWith( System.out::println) // вывод запроса и ответа
            .jsonPath( "$.login").isEqualTo( EXISTS_USER_LOGIN)
        ;
    }

}
