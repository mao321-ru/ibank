package com.example.ibank.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class IntegrationTestPostgres extends IntegrationTestBase {

    @DynamicPropertySource
    static void registerPostgresProperties( DynamicPropertyRegistry registry) {
        registry.add( "spring.r2dbc.url", () ->
                "r2dbc:postgresql://%s:%s/%s".formatted(
                        postgres.getHost(),
                        postgres.getFirstMappedPort(),
                        postgres.getDatabaseName()
                )
        );
    }

}
