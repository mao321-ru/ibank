package com.example.ibank.front.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

public class MainControllerTest extends ControllerTest {

    final String PASSWORD_ERROR_XPATH = "//*[@class='passwordError']";

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
    void main_ok() throws Exception {
        final String login = EXISTS_USER_LOGIN;
        final String password = EXISTS_USER_PASSWORD;

        String sessionCookie = checkLoginOk( login, password);
        wtc.get().uri( MAIN_URL)
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( "//*[@class='login']").isEqualTo( EXISTS_USER_LOGIN)
                .xpath( "//*[@class='userName']").isEqualTo( EXISTS_USER_NAME)
                .xpath( "//*[@class='birthDate']").isEqualTo(
                    LocalDate.parse( EXISTS_USER_BIRTHDATE).format( DateTimeFormatter.ofPattern( "dd.MM.yyyy"))
                )
                .xpath( "//*[@class='getAccountsError']").nodeCount( 0)
                .xpath( "//*[@class='userAccount']").nodeCount( CURRENCIES_COUNT)
                .xpath( "//*[@class='userAccount__curName'][1]").isEqualTo( CURRENCY_RUB_NAME)
                .xpath( "//*[@class='userAccount__valueText'][1]").isEqualTo(
                    EXISTS_USER_RUB_AMOUNT + " " + CURRENCY_RUB_CODE
                )
                .xpath( "//*[@class='eachCurrency']").nodeCount( CURRENCIES_COUNT)
                .xpath( "//*[@class='getUsersError']").nodeCount( 0)
                .xpath( "//*[@class='toUser']").nodeCount( Matchers.greaterThanOrEqualTo( 1))
                .xpath( "//*[@class='toUser'][@value='%s']".formatted( login)).nodeCount( 0)
        ;
    }

    @Test
    void editPassword_diffPwd() throws Exception {
        final String login = EXISTS_USER_LOGIN;
        final String password = EXISTS_USER_PASSWORD;

        // для проверки передачи текста ошибки через атрибуты сессии тестируемся с созданием реальной сессии
        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
            .post().uri( "/user/{login}/editPassword",login)
            .cookie("SESSION", sessionCookie)
            .contentType( MediaType.APPLICATION_FORM_URLENCODED)
            .body( BodyInserters
                    .fromFormData( "password", "pwd1")
                    .with( "confirm_password", "pwd2")
            )
            .exchange()
            .expectStatus().isSeeOther()
            .expectHeader().location( MAIN_URL)
            //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        // проверяем отображение ошибки в текущей сессии
        var res = wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( PASSWORD_ERROR_XPATH).nodeCount( 1)
                .xpath( PASSWORD_ERROR_XPATH).isEqualTo( "Указаны различные пароли")
                .returnResult()
        ;

        // обновляем sessionCookie если был указан в ответе
        sessionCookie = getSessionCookie( res, sessionCookie);

        // после перезагрузки страницы ошибка должна исчезнуть
        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( PASSWORD_ERROR_XPATH).nodeCount( 0)
        ;
    }

    @Test
    @WithMockUser( username = EXISTS_USER2_LOGIN)
    void editPassword_ok() throws Exception {
        final String login = EXISTS_USER2_LOGIN;
        final String newPassword = "editPassword_ok";

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/editPassword",login)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "password", newPassword)
                        .with( "confirm_password", newPassword)
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
                //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        // проверяем успешный вход под новым паролем
        checkLoginOk( login, newPassword);
    }

}
