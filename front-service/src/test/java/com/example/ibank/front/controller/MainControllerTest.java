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

}
