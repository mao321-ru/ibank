package com.example.ibank.common.confsrv;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;


// Общие настройки интеграционных тестов во всех модулях
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles( { "itest", "test" })
@AutoConfigureWebTestClient
public abstract class IntegrationTestBaseConfsrv {

    protected static final Network network = Network.newNetwork();

    protected static FixedHostPortGenericContainer confsrv;

    protected static void startConfsrv(
        int confsrvExposedPort
    ) {
        confsrv = new FixedHostPortGenericContainer<>( "local/ibank-confsrv:test")
            .withExposedPorts(8888)
            // открываемый порт должен совпадать с портом из spring.config.import в @TestPropertySource модуля
            .withFixedExposedPort( confsrvExposedPort, 8888)
            .withNetwork(network)
            .withNetworkAliases( "confsrv")
            //.withLogConsumer( new Slf4jLogConsumer(LoggerFactory.getLogger("TC-LOGS")))
            .waitingFor( Wait.forHttp("/actuator/health"));
        confsrv.start();
    }

}
