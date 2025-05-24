package com.example.ibank.front.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

public class MainControllerTest extends ControllerTest {

    @Test
    void root_noAuth() throws Exception {
        wtc.get().uri( "/")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals( "Location", "/login" )
        ;
    }

    @Test
    @WithMockUser( username = "user")
    void root_authRedirect() throws Exception {
        wtc.get().uri( "/")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().valueEquals( "Location", "/main" )
        ;
    }

    @Test
    void main_noAuth() throws Exception {
        wtc.get().uri( "/main")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals( "Location", "/login" )
        ;
    }

    @Test
    @WithMockUser( username = EXISTS_USER_LOGIN)
    void main_ok() throws Exception {
        wtc.get().uri( "/main")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( "//*[@class='login']").isEqualTo( EXISTS_USER_LOGIN)
        ;
    }

}
