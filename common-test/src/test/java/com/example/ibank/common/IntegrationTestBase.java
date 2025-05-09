package com.example.ibank.common;

import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;

import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;

// Общие настройки интеграционных тестов во всех модулях
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    protected enum Container {
        CONFSRV,
        EUREKA,
        GATEWAY
    };

    protected static EnumMap<Container,GenericContainer<?>> containers = new EnumMap<>( Container.class);

    protected static final Network network = Network.newNetwork();


    // Start containers and uses Ryuk Container to remove containers when JVM process running the tests exited
    protected static void startContainers(
        int confsrvExposedPort,
        List<Container> addonContainers
    ) {
        // всегда создаем, не нужно указывать в addonContainers
        var confsrv = new FixedHostPortGenericContainer<>( "local/ibank-confsrv:test")
                .withExposedPorts(8888)
                // открываемый порт должен совпадать с портом из spring.config.import в @TestPropertySource модуля
                .withFixedExposedPort( confsrvExposedPort, 8888)
                .withNetwork(network)
                .withNetworkAliases( "confsrv")
                //.withLogConsumer( new Slf4jLogConsumer(LoggerFactory.getLogger("TC-LOGS")))
                .waitingFor( Wait.forHttp("/actuator/health"));
        confsrv.start();
        containers.put( Container.CONFSRV, confsrv);

        // создание и запуск дополнительного контейнера если он указан в списке addonContainers
        BiConsumer<Container,Integer> startIfUsed = ( cntType, port) -> {
            if( addonContainers.contains( cntType)) {
                String cntName = cntType.toString().toLowerCase();
                containers.put(
                    cntType,
                    new GenericContainer<>(
                            "local/ibank-%s:test".formatted( cntName)
                        )
                        .withExposedPorts( port)
                        .withNetwork(network)
                        .withNetworkAliases( cntName)
                        .withEnv( "SPRING_CONFIG_IMPORT", "configserver:http://confsrv:8888")
                        .withEnv( "SPRING_PROFILES_ACTIVE", "docker")
                        //.withLogConsumer( new Slf4jLogConsumer(LoggerFactory.getLogger("TC-LOGS")))
                        .waitingFor( Wait.forHttp("/actuator/health"))
                );
                containers.get( cntType).start();
            }
        };

        startIfUsed.accept( Container.EUREKA, 8761);
        startIfUsed.accept( Container.GATEWAY, 8880);
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
