package com.example.ibank.eureka;

import com.example.ibank.common.IntegrationTestBase;

import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8912")
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( 8912, List.of());
    }

}
