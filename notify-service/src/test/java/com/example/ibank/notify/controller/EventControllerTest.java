package com.example.ibank.notify.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class EventControllerTest extends ControllerTest {

    @Test
    void createEvent_ok() throws Exception {
        wtc.post().uri( "/events")
            .headers( headers -> headers.setBearerAuth( getAccessToken( "accounts-service")))
            .contentType( MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                    "source": "accounts-service",
                    "eventType": "create_account",
                    "eventTime": "2023-05-16T09:15:22+04:00",
                    "userLogin": "createEvent_ok",
                    "message": "Открыт счет в RUB"
                }
                """
            )
            .exchange()
            .expectStatus().isNoContent()
        ;
    }

}
