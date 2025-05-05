package com.example.ibank.gateway;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

// Общие настройки для всех интеграционных тестов
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTest {

    static GenericContainer<?> configServer =
        new FixedHostPortGenericContainer<>( "ibank-confsrv:latest")
            .withExposedPorts(8888)
            .withFixedExposedPort(8988, 8888)
            .waitingFor( Wait.forHttp("/actuator/health"))
    ;

    // Start containers and uses Ryuk Container to remove containers when JVM process running the tests exited
    static {
        configServer.start();
    }

//    @DynamicPropertySource
//    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.cloud.config.uri", () ->
//                //"configserver:http://" + configServer.getHost() + ":" + configServer.getMappedPort(8888)
//                "http://localhost:9999"
//        );
//    }

}
