package com.example.ibank.common;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

public abstract class IntegrationTestBase {

    protected static final Network network = Network.newNetwork();

    protected static GenericContainer<?> eureka = new GenericContainer<>( "local/ibank-eureka:test")
            .withExposedPorts(8761)
            .withNetwork(network)
            .withNetworkAliases( "eureka")
            .withEnv("SPRING_PROFILES_ACTIVE", "intg-test")
            .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://confsrv:8888")
            .waitingFor( Wait.forHttp("/actuator/health"));

}
