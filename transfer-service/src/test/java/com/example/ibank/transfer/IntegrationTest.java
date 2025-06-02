package com.example.ibank.transfer;

import com.example.ibank.common.IntegrationTestBase;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8924")
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( 8924, List.of(
            Container.EUREKA,
            Container.GATEWAY,
            Container.ACCOUNTS_SERVICE,
            Container.BLOCKER_SERVICE,
            Container.NOTIFY_SERVICE
        ));
    }

}
