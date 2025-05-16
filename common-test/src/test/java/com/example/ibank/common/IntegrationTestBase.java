package com.example.ibank.common;

import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Assert;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;

// Общие настройки интеграционных тестов во всех модулях
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// использование @AutoConfigureWebTestClient приводит к ошибке в getAccessToken (см. ниже)
public abstract class IntegrationTestBase implements TestData {

    // необязательный чтобы не было ошибки в модуле eureka из-за отсутствия бина (wtc там не нужен)
    @Autowired( required = false)
    WebTestClient wtc;

    private static final Logger log = LoggerFactory.getLogger( IntegrationTestBase.class);

    protected enum Container {
        CONFSRV,
        EUREKA,
        GATEWAY,
        ACCOUNTS_SERVICE
    };

    protected static EnumMap<Container,GenericContainer<?>> containers = new EnumMap<>( Container.class);

    protected static final Network network = Network.newNetwork();

    protected static KeycloakContainer keycloak;

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

        // всегда создаем keycloak если есть Gateway
        if( addonContainers.contains( Container.GATEWAY)) {
            keycloak = new KeycloakContainer( "quay.io/keycloak/keycloak:26.1.3")
                .withNetwork(network)
                .withNetworkAliases( "keycloak")
                .withRealmImportFile("/keycloak/paysrv-test.realm.json")
            ;
            keycloak.start();
        }

        // создание и запуск дополнительного контейнера если он указан в списке addonContainers
        BiConsumer<Container,Integer> startIfUsed = ( cntType, port) -> {
            if( addonContainers.contains( cntType)) {
                String cntName = cntType.toString().toLowerCase().replace( "_", "-");
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

        startIfUsed.accept( Container.ACCOUNTS_SERVICE, 8080);

        // старт после запуска всех сервисов
        startIfUsed.accept( Container.GATEWAY, 8880);
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        if( keycloak != null) {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () ->
                    keycloak.getAuthServerUrl() + "/realms/paysrv-test"
            );
        }
        if( containers.containsKey( Container.EUREKA)) {
            registry.add("eureka.client.serviceUrl.defaultZone", () ->
                "http://localhost:%d/eureka/".formatted(
                        containers.get( Container.EUREKA).getMappedPort(8761)
                )
            );
        }
        if( containers.containsKey( Container.GATEWAY)) {
            registry.add("gateway.url", () ->
                "http://localhost:%d".formatted(
                    containers.get( Container.GATEWAY).getMappedPort(8880)
                )
            );
        }
    }

    protected String getAccessToken( String clientId) {
        Assert.notNull( wtc, "WebTestClient is not available in this module");
        // использование @AutoConfigureWebTestClient приводит к исчезновению части передаваемых заголовков
        // и ошибке 401 UNAUTHORIZED (явное указание заголовков проблему не решает)
        String jsonText = wtc.mutate()
            .baseUrl( keycloak.getAuthServerUrl()).build()
            .post()
            .uri("/realms/paysrv-test/protocol/openid-connect/token")
            .bodyValue(
                    "grant_type=client_credentials&client_id=%s&client_secret=**********".formatted( clientId)
            )
            .header("Content-Type", "application/x-www-form-urlencoded")
            .exchange()
            .expectBody(String.class)
            .consumeWith( System.out::println) // вывод запроса и ответа
            .consumeWith( r -> {
                assertThat( r.getStatus())
                    .withFailMessage( "Bad status on get Keycloak token for: " + clientId)
                    .isEqualTo( HttpStatus.OK);
            })
            .returnResult()
            .getResponseBody()
        ;
        log.debug( "get access token for: {}", clientId);
        return JsonPath.parse( jsonText).read( "$.access_token").toString();
    }

}
