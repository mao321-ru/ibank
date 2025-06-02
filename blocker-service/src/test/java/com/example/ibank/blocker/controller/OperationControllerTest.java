package com.example.ibank.blocker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class OperationControllerTest extends ControllerTest {

    @Test
    void checkOperation_ok() throws Exception {
        wtc.post().uri( "/operation/check")
                .headers( headers -> headers.setBearerAuth( getAccessToken( "cash-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                    """
                    {
                        "login": "%s",
                        "operationType": "DEPOSIT",
                        "amount": "100.05",
                        "currency": "RUB"
                    }
                    """.formatted( EXISTS_USER_LOGIN)
                )
                .exchange()
                .expectStatus().isNoContent()
                //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;
    }

    @Test
    void checkOperation_block() throws Exception {
        wtc.post().uri( "/operation/check")
                .headers( headers -> headers.setBearerAuth( getAccessToken( "cash-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                        {
                            "login": "%s",
                            "operationType": "DEPOSIT",
                            "amount": "1000000.05",
                            "currency": "RUB"
                        }
                        """.formatted( EXISTS_USER_LOGIN)
                )
                .exchange()
                .expectStatus().isEqualTo( HttpStatus.CONFLICT)
                .expectBody()
                //.consumeWith( System.out::println) // вывод запроса и ответа
                .json(
                    """
                    {
                        "error_code": 409,
                        "error_message": "Сумма по операции не должна превышать 10000 RUB"
                    }
                    """
                )
        ;
    }

}
