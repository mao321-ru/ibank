package com.example.ibank.front.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

public class MainControllerTest extends ControllerTest {

    final String ERROR_XPF = "//*[@class='%sError']";
    final String PASSWORD_ERROR_XPATH = "//*[@class='passwordError']";
    final String USER_ACCOUNTS_ERROR_XPATH = "//*[@class='userAccountsError']";
    final String USERNAME_XPATH = "//*[@class='userName']";
    final String BIRTHDATE_XPATH = "//*[@class='birthDate']";
    final String ACCOUNT_EXISTS_XPF = "//*[@class='userAccount__check'][@value='%s'][@checked='checked']";
    final String ACCOUNT_VALUE_RUB_TEXT_XPATH = "//*[@class='userAccount__valueText'][1]";

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
                .xpath( USERNAME_XPATH).isEqualTo( EXISTS_USER_NAME)
                .xpath( BIRTHDATE_XPATH).isEqualTo(
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
    @WithMockUser( username = DELETED_USER_LOGIN)
    void deleteUser_ok() throws Exception {
        final String login = DELETED_USER_LOGIN;
        final String password = DELETED_USER_PASSWORD;

        // проверяем что можем войти
        checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/deleteUser",login)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location( LOGIN_URL)
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        // проверяем что теперь не можем войти
        wtc.mutateWith( csrf())
                .post().uri( "/login")
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "username", login)
                        .with( "password", password)
                )
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location( LOGIN_ERROR_URL)
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

    @Test
    void editUserAccounts_ok() throws Exception {
        final String login = EDITED_USER_LOGIN;
        final String password = EDITED_USER_PASSWORD;

        final String userName = "editedUserAccounts_ok";
        final String birthDate = "2001-12-23";

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/editUserAccounts",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "name", userName)
                        .with( "birthdate", birthDate)
                        .with( "account", "USD")
                        .with( "account", "RUB")
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
            //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( USER_ACCOUNTS_ERROR_XPATH).nodeCount( 0)
                .xpath( USERNAME_XPATH).isEqualTo( userName)
                .xpath( BIRTHDATE_XPATH).isEqualTo(
                    LocalDate.parse( birthDate).format( DateTimeFormatter.ofPattern( "dd.MM.yyyy"))
                )
                .xpath( ACCOUNT_EXISTS_XPF.formatted( "RUB")).nodeCount( 1)
                .xpath( ACCOUNT_EXISTS_XPF.formatted( "USD")).nodeCount( 1)
                .xpath( ACCOUNT_EXISTS_XPF.formatted( "EUR")).nodeCount( 0)
        ;
    }

    @Test
    void editUserAccounts_nameOnly() throws Exception {
        final String login = EDITED_USER_LOGIN;
        final String password = EDITED_USER_PASSWORD;

        final String userName = "editedUserAccounts_nameOnly";

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/editUserAccounts",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters.fromFormData( "name", userName))
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
            //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( USER_ACCOUNTS_ERROR_XPATH).doesNotExist()
                .xpath( USERNAME_XPATH).isEqualTo( userName)
        ;
    }

    @Test
    void cashAction_ok() throws Exception {
        final String login = CASH_USER_LOGIN;
        final String password = CASH_USER_PASSWORD;

        final String currency = "RUB";
        final String putAmount = "100.91";
        final String amount = new BigDecimal( CASH_USER_RUB_AMOUNT).add( new BigDecimal( putAmount)).toString();

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/cash",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "currency", currency)
                        .with( "value", putAmount)
                        .with( "action", "PUT")
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
            //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( ERROR_XPF.formatted( "cash")).nodeCount( 0)
                .xpath( ACCOUNT_EXISTS_XPF.formatted( "RUB")).nodeCount( 1)
                .xpath( ACCOUNT_VALUE_RUB_TEXT_XPATH).isEqualTo("%s %s".formatted( amount, currency))
        ;
    }

    @Test
    void cashAction_get() throws Exception {
        final String login = CASH_USER_LOGIN;
        final String password = CASH_USER_PASSWORD;

        final String currency = "USD";
        final String getAmount = "150.00";
        final String amount = new BigDecimal( CASH_USER_USD_AMOUNT).subtract( new BigDecimal( getAmount)).toString();

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/cash",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "currency", currency)
                        .with( "value", getAmount)
                        .with( "action", "GET")
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( ERROR_XPF.formatted( "cash")).nodeCount( 0)
        ;
    }

    @Test
    void cashAction_noMoney() throws Exception {
        final String login = CASH_USER_LOGIN;
        final String password = CASH_USER_PASSWORD;

        final String currency = "RUB";
        final String getAmount = "250.00";

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/cash",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "currency", currency)
                        .with( "value", getAmount)
                        .with( "action", "GET")
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( ERROR_XPF.formatted( "cash"))
                    .isEqualTo( "Недостаточно средств на счете [RUB] для выполнения операции")
        ;
    }

    @Test
    void cashAction_noAccount() throws Exception {
        final String login = CASH_USER_LOGIN;
        final String password = CASH_USER_PASSWORD;

        final String currency = "EUR";
        final String putAmount = "5.00";

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/cash",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "currency", currency)
                        .with( "value", putAmount)
                        .with( "action", "PUT")
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( ERROR_XPF.formatted( "cash")).isEqualTo(
                    "Нужно открыть счет в [%s] для выполнения операции".formatted( currency)
                )
        ;
    }

    @Test
    void cashAction_block() throws Exception {
        final String login = CASH_USER_LOGIN;
        final String password = CASH_USER_PASSWORD;

        final String currency = "RUB";
        final String putAmount = "1000000.91";

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/cash",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "currency", currency)
                        .with( "value", putAmount)
                        .with( "action", "PUT")
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .xpath( ERROR_XPF.formatted( "cash")).isEqualTo(
                    "Сумма по операции не должна превышать 10000 RUB"
                )
        ;
    }

    @Test
    void transfer_ok() throws Exception {
        final String login = TRANSFER_USER_LOGIN;
        final String password = TRANSFER_USER_PASSWORD;

        final String fromCurrency = "RUB";
        final String toCurrency = "USD";
        final String amount = "400";
        final String restAmount = "600.00";

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/transfer",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "from_currency", fromCurrency)
                        .with( "to_currency", toCurrency)
                        .with( "value", amount)
                        .with( "to_login", login)
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                .consumeWith( System.out::println) // вывод запроса и ответа
                //.xpath( ERROR_XPF.formatted( "transferOther")).nodeCount( 0)
                .xpath( ERROR_XPF.formatted( "transfer")).string( Matchers.blankOrNullString())
                .xpath( ERROR_XPF.formatted( "transferOther")).string( Matchers.blankOrNullString())
                .xpath( ACCOUNT_VALUE_RUB_TEXT_XPATH).isEqualTo("%s %s".formatted( restAmount, fromCurrency))
        ;
    }

    @Test
    void transfer_toOther() throws Exception {
        final String login = TRANSFER2_USER_LOGIN;
        final String password = TRANSFER2_USER_PASSWORD;

        final String toLogin = TO_TRANSFER_USER_LOGIN;
        final String currency = "RUB";
        final String amount = "300";
        final String restAmount = "700.00";

        String sessionCookie = checkLoginOk( login, password);

        wtc.mutateWith( csrf())
                .post().uri( "/user/{login}/transfer",login)
                .cookie("SESSION", sessionCookie)
                .contentType( MediaType.APPLICATION_FORM_URLENCODED)
                .body( BodyInserters
                        .fromFormData( "from_currency", currency)
                        .with( "to_currency", currency)
                        .with( "value", amount)
                        .with( "to_login", toLogin)
                )
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location( MAIN_URL)
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( MAIN_URL)
                // используем cookie из предыдущего запроса
                .cookie("SESSION", sessionCookie)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType( "text/html;charset=UTF-8")
                .expectBody()
                .consumeWith( System.out::println) // вывод запроса и ответа
                //.xpath( ERROR_XPF.formatted( "transferOther")).nodeCount( 0)
                .xpath( ERROR_XPF.formatted( "transfer")).string( Matchers.blankOrNullString())
                .xpath( ERROR_XPF.formatted( "transferOther")).string( Matchers.blankOrNullString())
                .xpath( ACCOUNT_VALUE_RUB_TEXT_XPATH).isEqualTo("%s %s".formatted( restAmount, currency))
        ;
    }
}
