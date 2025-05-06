package com.example.ibank.eureka;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

// Общие настройки для всех интеграционных тестов
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8912")
public abstract class IntegrationTest {

    static GenericContainer<?> confsrv = new FixedHostPortGenericContainer<>( "ibank-confsrv:latest")
            .withExposedPorts(8888)
            // открываемый порт должен совпадать с портом из spring.config.import в @TestPropertySource (выше)
            .withFixedExposedPort(8912, 8888)
            .waitingFor( Wait.forHttp("/actuator/health"));

    // Start containers and uses Ryuk Container to remove containers when JVM process running the tests exited
    static {
        confsrv.start();
    }

}
