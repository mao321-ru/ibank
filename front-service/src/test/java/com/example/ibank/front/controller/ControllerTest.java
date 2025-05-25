package com.example.ibank.front.controller;

import com.example.ibank.front.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;


// Общие настройки и т.д. для интеграционныйх тестов контроллеров
public class ControllerTest extends IntegrationTest {

    protected final String MAIN_URL = "/main";

    @Autowired
    protected WebTestClient wtc;

    protected String getSessionCookie(
         EntityExchangeResult<byte[]>  res,
         String lastSessionCookie
    )
    {
        return Optional.ofNullable( res.getResponseCookies().get( "SESSION"))
            .map( t -> t.getFirst().getValue())
            .orElse( lastSessionCookie);
    }

    // проверяет успешный вход и возвращает cookie для работы в сессии через .cookie("SESSION", <cookie>)
    protected String checkLoginOk( String login, String password) {
        var res = wtc.mutateWith( csrf())
                .post().uri( "/login")
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "username", login)
                        .with( "password", password)
                )
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals( "Location", "/main")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .returnResult();

        String sessionCookie = getSessionCookie( res, null);
        assertNotNull( "login response: sessionCookie not found", sessionCookie);
        return sessionCookie;
    }
}
