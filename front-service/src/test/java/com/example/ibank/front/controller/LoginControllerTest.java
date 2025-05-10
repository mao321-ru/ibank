package com.example.ibank.front.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

public class LoginControllerTest extends ControllerTest {

    final String ERROR_INFO_XPATH = "//span[@class='errorInfo']";

    @Test
    void login_noAuth() throws Exception {
        wtc.get().uri( "/login")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                .xpath( "//form[@action='/login']").nodeCount( 1)
                .xpath( ERROR_INFO_XPATH).nodeCount( 0)
        ;
    }

    @Test
    void login_afterError() throws Exception {
        wtc.get().uri( "/login?error")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( ERROR_INFO_XPATH).nodeCount( 1)
        ;
    }

    @Test
    void login_noCsrf() throws Exception {
        wtc.post().uri( "/login")
            .contentType( MediaType.APPLICATION_FORM_URLENCODED)
            .body( BodyInserters
                .fromFormData( "username", EXISTS_USER_NAME)
                .with( "password", EXISTS_USER_PASSWORD)
            )
            .exchange()
            .expectStatus().isForbidden()
            .expectBody()
            //.consumeWith( System.out::println) // вывод запроса и ответа
            .consumeWith( b -> assertThat( b.getResponseBody()).asString()
                .contains( "An expected CSRF token cannot be found"))
        ;
    }

    @Test
    void login_authUser() throws Exception {
        wtc.mutateWith( csrf())
            .post().uri( "/login")
            .contentType( MediaType.APPLICATION_FORM_URLENCODED)
            .body( BodyInserters
                .fromFormData( "username", EXISTS_USER_NAME)
                .with( "password", EXISTS_USER_PASSWORD)
            )
            .exchange()
            .expectStatus().isFound()
            .expectHeader().valueEquals( "Location", "/")
        ;
    }

    @Test
    void signup_noAuth() throws Exception {
        wtc.get().uri( "/signup")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
        ;
    }

}
