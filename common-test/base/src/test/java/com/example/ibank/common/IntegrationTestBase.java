package com.example.ibank.common;

import com.example.ibank.common.confsrv.IntegrationTestBaseConfsrv;

import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.MountableFile;
import reactor.core.publisher.Mono;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import dasniko.testcontainers.keycloak.KeycloakContainer;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;

// использование @AutoConfigureWebTestClient приводит к ошибке в getAccessToken (см. ниже)
public abstract class IntegrationTestBase extends IntegrationTestBaseConfsrv implements TestData {

    static final String profilesActive = "docker,itest";
    static final String keycloakTestRealm = "ibank";
    static final String clientTestSecretTail = "-TestSecret";

    private static final Logger log = LoggerFactory.getLogger( IntegrationTestBase.class);

    protected enum Container {
        EUREKA,
        GATEWAY,
        POSTGRES,
        ACCOUNTS_SERVICE,
        CASH_SERVICE,
        TRANSFER_SERVICE,
        EXCHANGE_SERVICE,
        BLOCKER_SERVICE,
        NOTIFY_SERVICE
    };

    protected static EnumMap<Container,GenericContainer<?>> containers = new EnumMap<>( Container.class);

    protected static final int keycloakKcHttpPort = 8954;
    protected static KeycloakContainer keycloak;
    protected static String  keycloakUrl;
    protected static String  keycloakIssuerUrl;

    protected static PostgreSQLContainer postgres;

    private static void startGateway() {
        var cntType = Container.GATEWAY;
        int port = 8880;
        String cntName = cntType.toString().toLowerCase();
        containers.put(
            cntType,
            new GenericContainer<>( "local/ibank-%s:test".formatted( cntName))
                .withExposedPorts( port)
                .withNetwork(network)
                .withNetworkAliases( cntName)
                .withEnv( "SPRING_CONFIG_IMPORT", "configserver:http://confsrv:8888")
                .withEnv( "SPRING_PROFILES_ACTIVE", profilesActive)
                .withLogConsumer( new Slf4jLogConsumer( LoggerFactory.getLogger("TC-GATEWAY")))
                .waitingFor( Wait.forHttp("/actuator/health"))
        );
        containers.get( cntType).start();
    }

    // Start containers and uses Ryuk Container to remove containers when JVM process running the tests exited
    protected static void startContainers(
        int confsrvExposedPort,
        List<Container> addonContainers
    ) {
        // сервис конфигов всегда создаем, не нужно указывать в addonContainers
        startConfsrv( confsrvExposedPort);

        // всегда создаем keycloak если есть Gateway
        if( addonContainers.contains( Container.GATEWAY)) {
            keycloak = new KeycloakContainer( "quay.io/keycloak/keycloak:26.1.3")
                .withNetwork(network)
                // явно задаем порт запуска keycloak в контейнере вместо 8080
                .withEnv( "KC_HTTP_PORT", Integer.toString( keycloakKcHttpPort))
                // открываем явно заданный порт вместо 8080 + стандартный порт healthcheck
                .withExposedPorts( keycloakKcHttpPort, 9000)
                .withNetworkAliases( "keycloak")
                .withRealmImportFile( "/keycloak/" + keycloakTestRealm + ".realm.json")
            ;
            keycloak.start();
            keycloakIssuerUrl = "http://keycloak:8954";
            log.info( "keycloakIssuerUrl: {}", keycloakIssuerUrl);
        }

        // всегда создаем postgres если указан явно либо есть использующие его сервисы
        if(
            addonContainers.contains( Container.POSTGRES)
            || addonContainers.contains( Container.ACCOUNTS_SERVICE)
        ) {
            postgres = (PostgreSQLContainer)
                new PostgreSQLContainer( "postgres:17.2-alpine3.20")
                        .withUsername( "postgres")
                        .withPassword( "postgres")
                        .withDatabaseName( "unused_db")
                        .withNetwork( network)
                        .withNetworkAliases( "postgres")
                        .withCopyFileToContainer(
                                MountableFile.forHostPath( "../postgres/init/"), "/docker-entrypoint-initdb.d/"
                        )
                // так тоже можно (найденный файлы будут добавлены)
                //.withCopyFileToContainer(
                //    //MountableFile.forClasspathResource( "/db/init/accounts_ibd.sql"), "/docker-entrypoint-initdb.d/"
                //    MountableFile.forClasspathResource( "/db/init/"), "/docker-entrypoint-initdb.d/"
                //)
                // логирование для контейнера
                //.withLogConsumer( new Slf4jLogConsumer( LoggerFactory.getLogger("T^C-LOGS")))
            ;
            postgres.start();
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
                        .withEnv( "KEYCLOAK_ISSUER_URL", keycloakIssuerUrl)
                        .withEnv( "SPRING_CONFIG_IMPORT", "configserver:http://confsrv:8888")
                        .withEnv( "SPRING_PROFILES_ACTIVE", profilesActive)
                        .withLogConsumer( new Slf4jLogConsumer( LoggerFactory.getLogger("TC-LOGS")))
                        .waitingFor( Wait.forHttp("/actuator/health"))
                );
                containers.get( cntType).start();
            }
        };

        startIfUsed.accept( Container.EUREKA, 8761);

        startIfUsed.accept( Container.NOTIFY_SERVICE, 8080);
        startIfUsed.accept( Container.ACCOUNTS_SERVICE, 8080);
        startIfUsed.accept( Container.BLOCKER_SERVICE, 8080);
        startIfUsed.accept( Container.CASH_SERVICE, 8080);
        startIfUsed.accept( Container.EXCHANGE_SERVICE, 8080);
        startIfUsed.accept( Container.TRANSFER_SERVICE, 8080);

        // старт после запуска всех сервисов, иначе могут быть ошибки вида
        // [gateway] o.s.c.l.core.RoundRobinLoadBalancer : No servers available for service: accounts-service
        if( addonContainers.contains( Container.GATEWAY)) {
            startGateway();
            // получаем токен не напрямую из keycloak через localhost, а запросом через gateway, который обратится
            // к keycloak через сеть докера через url "http://keycloak:8954", который попадет в iss возвращаемого
            // от keycloak токена. В результате iss токена будет совпадать с ожидаемымым (указанным keycloakIssuerUrl)
            // и авторизация пройдет успешно
            keycloakUrl = "http://localhost:%s/api/keycloak".formatted(
                containers.get( Container.GATEWAY).getMappedPort( 8880).toString()
            );
            log.info( "keycloakUrl: {}", keycloakUrl);
        }

    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        if( keycloak != null) {
            registry.add("keycloak.realm", () -> keycloakTestRealm);
            registry.add("keycloak.url", () -> keycloakUrl);
            registry.add("keycloak.issuer.url", () -> keycloakIssuerUrl);
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
        // вывод HTTP-запроса и ответа к keycloak
        final boolean isDebug = false;
        final String clientSecret = clientId + clientTestSecretTail;

        String requestBody = "grant_type=client_credentials" +
            "&client_id=" + clientId +
            "&client_secret=" + clientSecret
        ;
        // при одновременном использовании WebTestClient и @AutoConfigureWebTestClient запрос к keycloak завершался
        // ошибкой 401 UNAUTHORIZED, поэтому стал использовать WebClient с "ручным" логированием запроса
        WebClient webClient = WebClient.builder()
            .filter((request, next) -> {
                if( isDebug) {
                    System.out.println("\n> " + request.method() + " " + request.url());
                    request.headers().forEach((name, values) ->
                        values.forEach(value -> System.out.println("> " + name + ": " + value))
                    );
                    System.out.println();
                    System.out.println(requestBody);
                    return next.exchange(request)
                        .doOnNext(response -> {
                            System.out.println("\n< " + response.statusCode());
                            response.headers().asHttpHeaders().forEach((name, values) ->
                                    values.forEach(value -> System.out.println("< " + name + ": " + value)));
                        });
                }
                else {
                    return next.exchange(request);
                }
            })
            .build();

        String jsonText = webClient.post()
            .uri(keycloakUrl + "/realms/" + keycloakTestRealm + "/protocol/openid-connect/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue( requestBody)
            .exchangeToMono(clientResponse -> {
                if ( !clientResponse.statusCode().is2xxSuccessful()) {
                    return clientResponse.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(errorBody -> Mono.error(new RuntimeException(
                            "Keycloak error: " + clientResponse.statusCode() + " - " + errorBody)));
                }
                return clientResponse.bodyToMono(String.class);
            })
            .doOnNext( body -> { if( isDebug) System.out.println("\n" + body + "\n"); })
            .block();

        log.debug( "get access token for: {}", clientId);
        return JsonPath.parse( jsonText).read( "$.access_token").toString();
    }

}