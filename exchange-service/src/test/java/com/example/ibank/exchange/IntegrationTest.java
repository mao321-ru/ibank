package com.example.ibank.exchange;

import com.example.ibank.common.IntegrationTestPostgres;

import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8925")
public abstract class IntegrationTest extends IntegrationTestPostgres {

    static {
        startContainers( 8925, List.of(
            Container.POSTGRES
        ));
    }

}
