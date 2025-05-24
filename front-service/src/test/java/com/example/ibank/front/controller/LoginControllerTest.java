package com.example.ibank.front.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

public class LoginControllerTest extends ControllerTest {

    // URL для перехода в случае ошибки при логине
    final String LOGIN_ERROR_URL = "/login?error";
    final String ERROR_INFO_XPATH = "//*[@class='errorInfo']";

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
    void login_noCsrf() throws Exception {
        wtc.post().uri( "/login")
            .contentType( MediaType.APPLICATION_FORM_URLENCODED)
            .body( BodyInserters
                .fromFormData( "username", EXISTS_USER_LOGIN)
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
    void login_ok() throws Exception {
        checkLoginOk( EXISTS_USER_LOGIN, EXISTS_USER_PASSWORD);
    }

    @Test
    void login_badPassword() throws Exception {
        wtc.mutateWith( csrf())
                .post().uri( "/login")
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "username", EXISTS_USER_LOGIN)
                        .with( "password", EXISTS_USER_PASSWORD + "_bad")
                )
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location( LOGIN_ERROR_URL)
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

    @Test
    void signup_diffPwd() throws Exception {
        wtc.mutateWith( csrf())
                .post().uri( "/signup")
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "login", "signup_diffPwdUser")
                        .with( "password", "signupOkPwd")
                        .with( "confirm_password", "signupOkBad")
                        .with( "name", "Sugnup OkUser")
                        .with( "birthdate", "1970-03-28")
                )
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( ERROR_INFO_XPATH).nodeCount( 1)
                .xpath( ERROR_INFO_XPATH).isEqualTo( "Указаны различные пароли")
        ;
    }

    @Test
    void signup_ok() throws Exception {
        final String login = "signupOkUser";
        final String password = "signupOkPwd";

        var resp = wtc.mutateWith( csrf())
                .post().uri( "/signup")
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "login", login)
                        .with( "password", password)
                        .with( "confirm_password", password)
                        .with( "name", "Sugnup OkUser")
                        .with( "birthdate", "1970-03-28")
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().valueEquals( "Location", "/main")
                .expectCookie().exists("SESSION") // Проверяем наличие сессионной cookie
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .returnResult()
        ;

        // текущая сессия авторизована под созданным пользователем (доступен защищенный ресурс)
        wtc.get().uri("/main")
                // используем cookie из предыдущего запроса
                .cookie("SESSION", resp.getResponseCookies().get( "SESSION").getFirst().getValue())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        // можно залогиниться под созданным пользователем
        checkLoginOk( login, password);
    }

    // проверяет успешный вход
    void checkLoginOk( String login, String password ) {
        wtc.mutateWith( csrf())
                .post().uri( "/login")
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "username", login)
                        .with( "password", password)
                )
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals( "Location", "/")
        ;
    }

}
