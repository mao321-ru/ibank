package com.example.ibank.cash;

import com.example.ibank.common.IntegrationTestPostgres;

import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8923")
public abstract class IntegrationTest extends IntegrationTestPostgres {

    static {
        startContainers( 8923, List.of(
            Container.EUREKA,
            Container.GATEWAY,
            Container.ACCOUNTS_SERVICE,
            Container.NOTIFY_SERVICE
        ));
    }

}
