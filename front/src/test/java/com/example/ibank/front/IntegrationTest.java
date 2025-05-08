package com.example.ibank.front;

import com.example.ibank.common.IntegrationTestBase;

import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8921")
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( 8921, List.of(
            Container.EUREKA,
            Container.GATEWAY
        ));
    }

}
