package com.example.ibank.gateway;

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
public abstract class IntegrationTest {

    private static final Network network = Network.newNetwork();

    static GenericContainer<?> confsrv = new FixedHostPortGenericContainer<>( "ibank-confsrv:latest")
        .withExposedPorts(8888)
        // открываемый порт должен совпадать с портом из spring.config.import в @TestPropertySource (выше)
        .withFixedExposedPort(8913, 8888)
        .withNetwork(network)
        .withNetworkAliases( "confsrv")
        .waitingFor( Wait.forHttp("/actuator/health"));

    static GenericContainer<?> eureka = new GenericContainer<>( "ibank-eureka:latest")
        .withExposedPorts(8761)
        .withNetwork(network)
        .withNetworkAliases( "eureka")
        .withEnv("SPRING_PROFILES_ACTIVE", "intg-test")
        .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://confsrv:8888")
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
