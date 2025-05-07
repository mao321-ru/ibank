package com.example.ibank.common;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.EnumMap;
import java.util.List;

// Общие настройки интеграционных тестов во всех модулях
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    protected enum Container {
        CONFSRV,
        EUREKA
    };

    protected static EnumMap<Container,GenericContainer<?>> containers = new EnumMap<>( Container.class);

    protected static final Network network = Network.newNetwork();


    // Start containers and uses Ryuk Container to remove containers when JVM process running the tests exited
    protected static void startContainers(
        int confsrvExposedPort,
        List<Container> addonContainers
    ) {
        GenericContainer<?> container = null;
        // всегда создаем, не нежно указывать в addonContainers
        container = new FixedHostPortGenericContainer<>( "local/ibank-confsrv:test")
                .withExposedPorts(8888)
                // открываемый порт должен совпадать с портом из spring.config.import в @TestPropertySource модуля
                .withFixedExposedPort( confsrvExposedPort, 8888)
                .withNetwork(network)
                .withNetworkAliases( "confsrv")
                .waitingFor( Wait.forHttp("/actuator/health"));
        container.start();
        containers.put( Container.CONFSRV, container);

        if( addonContainers.contains( Container.EUREKA)) {
            container = new GenericContainer<>( "local/ibank-eureka:test")
                    .withExposedPorts(8761)
                    .withNetwork(network)
                    .withNetworkAliases( "eureka")
                    .withEnv("SPRING_PROFILES_ACTIVE", "intg-test")
                    .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://confsrv:8888")
                    .waitingFor( Wait.forHttp("/actuator/health"));
            container.start();
            containers.put( Container.EUREKA, container);
        }
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        if( containers.containsKey( Container.EUREKA)) {
            registry.add("eureka.client.serviceUrl.defaultZone", () ->
                "http://localhost:%d/eureka/".formatted(
                        containers.get( Container.EUREKA).getMappedPort(8761)
                )
            );
        }
    }

}
