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
            .contentType( MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted( EXISTS_USER_NAME, EXISTS_USER_PASSWORD)
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            //.consumeWith( System.out::println) // вывод запроса и ответа
            .jsonPath( "$.userId").isEqualTo( EXISTS_USER_NAME)
        ;
    }

}
