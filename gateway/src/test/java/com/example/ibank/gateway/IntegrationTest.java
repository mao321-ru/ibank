package com.example.ibank.gateway;


import com.example.ibank.common.IntegrationTestBase;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

// Общие настройки для всех интеграционных тестов
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8913")
public abstract class IntegrationTest extends IntegrationTestBase {

    static GenericContainer<?> confsrv = new FixedHostPortGenericContainer<>( "local/ibank-confsrv:test")
        .withExposedPorts(8888)
        // открываемый порт должен совпадать с портом из spring.config.import в @TestPropertySource (выше)
        .withFixedExposedPort(8913, 8888)
        .withNetwork(network)
        .withNetworkAliases( "confsrv")
        .waitingFor( Wait.forHttp("/actuator/health"));

    // Start containers and uses Ryuk Container to remove containers when JVM process running the tests exited
    static {
        confsrv.start();
        eureka.start();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.serviceUrl.defaultZone", () ->
            "http://localhost:%d/eureka/".formatted( eureka.getMappedPort( 8761))
        );
    }

}
