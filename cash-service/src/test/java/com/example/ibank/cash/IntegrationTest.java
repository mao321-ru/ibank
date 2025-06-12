package com.example.ibank.cash;

import com.example.ibank.common.IntegrationTestBase;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( List.of(
            Container.ACCOUNTS_SERVICE,
            Container.BLOCKER_SERVICE,
            Container.NOTIFY_SERVICE
        ));
    }

}
