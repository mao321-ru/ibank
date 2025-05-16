package com.example.ibank.front;

import com.example.ibank.common.IntegrationTestBase;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8921")
@AutoConfigureWebTestClient
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( 8921, List.of(
            Container.EUREKA,
            Container.GATEWAY,
            Container.ACCOUNTS_SERVICE
        ));
    }

}
