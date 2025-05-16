package com.example.ibank.eureka;

import com.example.ibank.common.confsrv.IntegrationTestBaseConfsrv;
import org.springframework.test.context.TestPropertySource;


// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8912")
public abstract class IntegrationTest extends IntegrationTestBaseConfsrv {

    static {
        startConfsrv( 8912);
    }

}
