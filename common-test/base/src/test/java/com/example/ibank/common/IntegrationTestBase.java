package com.example.ibank.common;

import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.MountableFile;
import reactor.core.publisher.Mono;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import dasniko.testcontainers.keycloak.KeycloakContainer;

import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;

// Общие настройки интеграционных тестов во всех модулях
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles( { "itest", "test" })
@AutoConfigureWebTestClient
// использование @AutoConfigureWebTestClient приводит к ошибке в getAccessToken (см. ниже)
public abstract class IntegrationTestBase implements TestData {

    static final String profilesActive = "docker,itest";
    static final String keycloakTestRealm = "ibank";
    static final String clientTestSecretTail = "-TestSecret";

    private static final Logger log = LoggerFactory.getLogger( IntegrationTestBase.class);

    protected enum Container {
        KEYCLOAK,
        POSTGRES,
        KAFKA,
        ACCOUNTS_SERVICE,
        CASH_SERVICE,
        TRANSFER_SERVICE,
        EXCHANGE_SERVICE,
        EXRATE_SERVICE,
        BLOCKER_SERVICE,
        NOTIFY_SERVICE
    };

    protected static final Network network = Network.newNetwork();

    protected static EnumMap<Container,GenericContainer<?>> containers = new EnumMap<>( Container.class);

    protected static final int keycloakKcHttpPort = 8954;
    protected static KeycloakContainer keycloak;
    protected static String  keycloakUrl;
    protected static String  keycloakIssuerUrl;
    protected static GenericContainer<?> keycloakProxy;

    protected static PostgreSQLContainer postgres;

    protected static KafkaContainer kafka;

    private static void startKeycloak() {
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
        // получаем токен не напрямую из keycloak через localhost, а запросом к прокси, который обратится
        // к keycloak по сети докера через url "http://keycloak:8954", и этот url попадет в iss возвращаемого
        // от keycloak токена. В результате iss токена будет совпадать с ожидаемымым (указанным keycloakIssuerUrl)
        // и авторизация пройдет успешно
        keycloakProxy = new GenericContainer<>("nginx:1.28.0-alpine3.21")
            .withNetwork(network)
            .withNetworkAliases("keycloak-proxy")
            .withExposedPorts(8080)
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("/keycloak/nginx.conf"), // Конфиг Nginx
                "/etc/nginx/nginx.conf"
            )
            .waitingFor( Wait.forHttp("/realms/" + keycloakTestRealm).forPort(8080))
        ;
        keycloakProxy.start();
        keycloakUrl = "http://localhost:%s".formatted( keycloakProxy.getMappedPort( 8080).toString());
        log.info( "keycloakUrl: {}", keycloakUrl);
    }

    private static void startKafka() {
        kafka = new KafkaContainer( "apache/kafka:4.0.0")
            .withNetwork(network)
            .withNetworkAliases( "kafka")
            // этот внешний адрес сообщается сервером и используется клиентом после переподключения например
            .withListener( "kafka:19092")
            .withCopyFileToContainer(
                MountableFile.forHostPath( "../kafka/config/"), "/mnt/shared/config/"
            )
        ;
        kafka.start();
        log.info( "kafka bootstrap servers: {}", kafka.getBootstrapServers());
    }

    // Start containers and uses Ryuk Container to remove containers when JVM process running the tests exited
    protected static void startContainers(
        List<Container> addonContainers
    ) {

        // создаем если есть кроме перечисленных (которым не нужен keycloak)
        if(
            addonContainers.stream().anyMatch( cn ->
                cn != Container.KAFKA
                && cn != Container.POSTGRES
                && cn != Container.NOTIFY_SERVICE
            )
        ) {
            startKeycloak();
        }

        // создаем контейнер если указан явно либо есть использующие его сервисы
        if(
            addonContainers.contains( Container.KAFKA)
            || addonContainers.contains( Container.EXCHANGE_SERVICE)
            || addonContainers.contains( Container.NOTIFY_SERVICE)
        ) {
            startKafka();
        }

        // всегда создаем postgres если указан явно либо есть использующие его сервисы
        if(
            addonContainers.contains( Container.POSTGRES)
            || addonContainers.contains( Container.ACCOUNTS_SERVICE)
            || addonContainers.contains( Container.EXCHANGE_SERVICE)
            || addonContainers.contains( Container.NOTIFY_SERVICE)
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
                        .withCopyFileToContainer(
                            MountableFile.forHostPath( "../config/data"), "/app.config/"
                        )
                        .withEnv( "KEYCLOAK_ISSUER_URL", keycloakIssuerUrl)
                        .withEnv( "SPRING_PROFILES_ACTIVE", profilesActive)
                        .withEnv( "CONFIG_DIR", "/app.config")
                        .withLogConsumer( new Slf4jLogConsumer( LoggerFactory.getLogger("TC-" + cntName)))
                        .waitingFor( Wait.forHttp("/actuator/health"))
                );
                containers.get( cntType).start();
            }
        };

        startIfUsed.accept( Container.NOTIFY_SERVICE, 8080);
        startIfUsed.accept( Container.ACCOUNTS_SERVICE, 8080);
        startIfUsed.accept( Container.BLOCKER_SERVICE, 8080);
        startIfUsed.accept( Container.EXCHANGE_SERVICE, 8080);
        startIfUsed.accept( Container.EXRATE_SERVICE, 8080);
        startIfUsed.accept( Container.CASH_SERVICE, 8080);
        startIfUsed.accept( Container.TRANSFER_SERVICE, 8080);
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        if( keycloak != null) {
            registry.add("keycloak.realm", () -> keycloakTestRealm);
            registry.add("keycloak.url", () -> keycloakUrl);
            registry.add("keycloak.issuer.url", () -> keycloakIssuerUrl);
        }
        if( kafka != null) {
            registry.add( "kafka_servers", () -> kafka.getBootstrapServers());
        }
        containers.forEach( ( cntType, cnt) -> {
            registry.add(
                cntType.toString().toLowerCase().replaceFirst( "_.+", ".url"),
                () -> "http://localhost:%d".formatted( cnt.getFirstMappedPort())
            );
        });
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