package com.example.ibank.front.controller;

import org.junit.jupiter.api.Test;

public class LoginControllerTest extends ControllerTest {

    @Test
    void login_noAuth() throws Exception {
        wtc.get().uri( "/login")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html")
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
