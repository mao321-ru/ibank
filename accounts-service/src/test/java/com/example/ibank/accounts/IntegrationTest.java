package com.example.ibank.accounts;

import com.example.ibank.common.IntegrationTestPostgres;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
public abstract class IntegrationTest extends IntegrationTestPostgres {

    static {
        startContainers( List.of(
            Container.KEYCLOAK,
            Container.POSTGRES,
            Container.NOTIFY_SERVICE
        ));
    }

}
