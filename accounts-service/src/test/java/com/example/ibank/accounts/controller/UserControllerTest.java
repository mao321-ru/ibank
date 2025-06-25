package com.example.ibank.accounts.controller;

import com.example.ibank.accounts.model.UserAccounts;
import com.example.ibank.accounts.model.UserShort;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class UserControllerTest extends ControllerTest {

    @Test
    void listUsers_ok() throws Exception {
        wtc.get().uri( "/users")
            .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
            .exchange()
            .expectStatus().isOk()
            .expectBodyList( UserShort.class)
            .value( list -> {
                //System.out.println( "list: " + list.toString());
                assertThat( list.size())
                    .as( "Check users list size")
                    .isGreaterThanOrEqualTo( 2);
            })
        ;
    }

    @Test
    void getUserAccounts_ok() throws Exception {
        wtc.get().uri( "/users/{login}", EXISTS_USER_LOGIN)
                .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
                .exchange()
                .expectStatus().isOk()
                .expectBody( UserAccounts.class)
                .value( res -> {
                    //System.out.println( "response: " + res.toString());
                    assertThat( res.getAccounts().size())
                        .as( "Check accounts count")
                        .isEqualTo( CURRENCIES_COUNT);
                })
        ;
    }

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
                            "name": "%s",
                            "birthDate": "%s"
                        }
                        """.formatted( login, userName, birthDate)
                )
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .jsonPath( "$.login").isEqualTo( login)
                .jsonPath( "$.name").isEqualTo( userName)
                .jsonPath( "$.birthDate").isEqualTo( birthDate)
        ;
    }

    @Test
    void createUser_usedLogin() throws Exception {
        final String login = EXISTS_USER_LOGIN;
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
                            "name": "%s",
                            "birthDate": "%s"
                        }
                        """.formatted( login, userName, birthDate)
                )
                .exchange()
                .expectStatus().isEqualTo( HttpStatus.CONFLICT)
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .json(
                        """
                        {
                            "error_code": 409,
                            "error_message": "Login [%s] already used"
                        }
                        """.formatted( login)
                )
        ;
    }

    @Test
    void deleteUser_ok() throws Exception {
        wtc.delete().uri( "/users/{login}", DELETED_USER_LOGIN)
            .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
            .exchange()
            .expectStatus().isNoContent()
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
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
            .jsonPath( "$.name").isEqualTo( EXISTS_USER_NAME)
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
        log.info( "waiting 0.5 seconds after send event ...");
        Thread.sleep( 500);
    }

    @Test
    void updateUserAccounts_ok() throws Exception {
        final String login = EDITED_USER_LOGIN;
        final String userName = "Update Step1";
        final String birthDate = "2010-01-21";

        String accessToken = getAccessToken( "front-service");

        wtc.patch().uri( "/users/{login}", login)
                .headers( headers -> headers.setBearerAuth( accessToken))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                    """
                    {
                        "name": "%s",
                        "birthDate": "%s",
                        "currencies": ["USD"]
                    }
                    """.formatted( userName, birthDate)
                )
                .exchange()
                .expectStatus().isNoContent()
                //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;

        wtc.get().uri( "/users/{login}", login)
                .headers( headers -> headers.setBearerAuth( accessToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(
                    """
                    {
                        "login":"%s",
                        "name":"%s",
                        "birthDate":"%s",
                        "accounts":[
                            {
                                "currency":{"code":"RUB","name":"Российский рубль"},
                                "value":0,
                                "exists":false
                            },
                            {
                                "currency":{"code":"USD","name":"Доллар США"},
                                "value":0,
                                "exists":true
                            },
                            {
                                "currency":{"code":"EUR","name":"Евро"},
                                "value":0,
                                "exists":false
                            }
                        ]
                    }
                    """.formatted( login, userName, birthDate)
                )
        ;
    }

    @Test
    void updateUserAccounts_notFound() throws Exception {
        final String login = "not_exists_user";

        wtc.patch().uri( "/users/{login}", login)
            .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
            .contentType( MediaType.APPLICATION_JSON)
            .bodyValue(
                    """
                    {
                        "name": "NotExists User",
                        "birthDate": "2010-01-21",
                        "currencies": ["USD"]
                    }
                    """
            )
            .exchange()
            .expectStatus().isNotFound()
            //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;
    }

    @Test
    void updateUserAccounts_closeNonZero() throws Exception {
        final String login = EXISTS_USER_LOGIN;

        wtc.patch().uri( "/users/{login}", login)
                .headers( headers -> headers.setBearerAuth( getAccessToken( "front-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                        {
                            "name": "updateUserAccount CloseNonZero",
                            "birthDate": "2010-01-21",
                            "currencies": ["USD","EUR"]
                        }
                        """
                )
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .json(
                    """
                    {
                        "error_code": 409,
                        "error_message": "Balance must be zero for close RUB account"
                    }
                    """
                )
        ;
    }

    @Test
    void updateUserAccounts_nullable() throws Exception {
        final String login = EDITED_USER_LOGIN;

        String accessToken = getAccessToken( "front-service");

        wtc.patch().uri( "/users/{login}", login)
                .headers( headers -> headers.setBearerAuth( accessToken))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                        {
                            "name": null,
                            "birthDate": null,
                            "currencies": ["EUR"]
                        }
                        """
                )
                .exchange()
                .expectStatus().isNoContent()
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;
    }
}
