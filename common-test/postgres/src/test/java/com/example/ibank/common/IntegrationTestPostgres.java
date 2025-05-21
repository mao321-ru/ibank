package com.example.ibank.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class IntegrationTestPostgres extends IntegrationTestBase {

    private static final Logger log = LoggerFactory.getLogger( IntegrationTestPostgres.class);

    @DynamicPropertySource
    static void registerPostgresProperties( DynamicPropertyRegistry registry) {
        registry.add( "postgres_host", () -> postgres.getHost());
        registry.add( "postgres_port", () -> postgres.getFirstMappedPort());
    }

}
