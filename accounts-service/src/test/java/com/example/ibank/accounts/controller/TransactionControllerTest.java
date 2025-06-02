package com.example.ibank.accounts.controller;

import com.example.ibank.accounts.model.UserAccounts;
import com.example.ibank.accounts.model.UserShort;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransactionControllerTest extends ControllerTest {

    @Test
    void createCashTransaction_ok() throws Exception {
        final String login = CASH_USER_LOGIN;

        wtc.post().uri( "/transactions/cash")
                .headers( headers -> headers.setBearerAuth( getAccessToken( "cash-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                    """
                    {
                        "login": "%s",
                        "amount": "50.01",
                        "currency": "RUB"
                    }
                    """.formatted(login)
                )
                .exchange()
                .expectStatus().isNoContent()
                //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;
    }

    @Test
    void createTransferTransaction_ok() throws Exception {
        final String login = TRANSFER_USER_LOGIN;
        final String toLogin = TO_TRANSFER_USER_LOGIN;

        wtc.post().uri( "/transactions/transfer")
                .headers( headers -> headers.setBearerAuth( getAccessToken( "transfer-service")))
                .contentType( MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                        {
                            "login": "%s",
                            "amount": "50.01",
                            "currency": "RUB",
                            "toLogin": "%s",
                            "toAmount": "1.01",
                            "toCurrency": "EUR"
                        }
                        """.formatted( login, toLogin)
                )
                .exchange()
                .expectStatus().isNoContent()
        //.expectBody().consumeWith( System.out::println) // вывод запроса и ответа
        ;
    }

}
