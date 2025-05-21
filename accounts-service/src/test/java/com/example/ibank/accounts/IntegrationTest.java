package com.example.ibank.accounts;

import com.example.ibank.common.IntegrationTestPostgres;

import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8922")
public abstract class IntegrationTest extends IntegrationTestPostgres {

    static {
        startContainers( 8922, List.of(
            Container.EUREKA,
            Container.GATEWAY,
            Container.POSTGRES
        ));
    }

}
