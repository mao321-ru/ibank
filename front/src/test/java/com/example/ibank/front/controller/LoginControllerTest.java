package com.example.ibank.front.controller;

import org.junit.jupiter.api.Test;

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
    void signup_noAuth() throws Exception {
        wtc.get().uri( "/signup")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
        ;
    }

}
