package com.example.ibank.blocker;

import com.example.ibank.common.IntegrationTestBase;

import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8927")
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( 8927, List.of(
            Container.EUREKA,
            Container.GATEWAY
        ));
    }

}
