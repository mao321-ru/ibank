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
            .jsonPath( "$.userName").isEqualTo( EXISTS_USER_NAME)
            .jsonPath( "$.birthDate").isEqualTo( EXISTS_USER_BIRTHDATE)
        ;
    }

    @Test
    void register_ok() throws Exception {
        final String login = "register_okUser";
        wtc.post().uri( "/auth/register")
                .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                        {
                            "login": "%s",
                            "password": "jjj",
                            "userName": "Register Ok",
                            "birthDate": "1990-01-15"                            
                        }
                        """.formatted( login)
                )
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .jsonPath( "$.login").isEqualTo( login)
        ;
    }

    @Test
    void changePassword_ok() throws Exception {
        wtc.post().uri( "/auth/password/change")
                .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                        {
                            "login": "%s",
                            "password": "%s"
                        }
                        """.formatted( EXISTS_USER2_LOGIN, "changePassword_ok")
                )
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith( System.out::println) // вывод запроса и ответа
        ;
    }
}
