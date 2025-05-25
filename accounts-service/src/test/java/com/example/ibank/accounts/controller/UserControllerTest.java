package com.example.ibank.accounts.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

public class UserControllerTest extends ControllerTest {

    @Test
    void createUser_ok() throws Exception {
        final String login = "register_okUser";
        final String userName = "Register Ok";
        final String birthDate = "1990-01-15";
        wtc.post().uri( "/users")
                .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                        {
                            "login": "%s",
                            "password": "jjj",
                            "userName": "%s",
                            "birthDate": "%s"
                        }
                        """.formatted( login, userName, birthDate)
                )
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .jsonPath( "$.login").isEqualTo( login)
                .jsonPath( "$.userName").isEqualTo( userName)
                .jsonPath( "$.birthDate").isEqualTo( birthDate)
        ;
    }

    @Test
    void validate_ok() throws Exception {
        wtc.post().uri( "/users/{login}/validate", EXISTS_USER_LOGIN)
            .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
            .contentType( MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "password": "%s"
                }
                """.formatted( EXISTS_USER_PASSWORD)
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
    void changePassword_ok() throws Exception {
        wtc.put().uri( "/users/{login}/password", EXISTS_USER2_LOGIN)
                .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                    """
                    {
                        "password": "%s"
                    }
                    """.formatted( "changePassword_ok")
                )
                .exchange()
                .expectStatus().isNoContent()
                //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;
    }
}
