package com.example.ibank.blocker;

import com.example.ibank.common.IntegrationTestBase;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( List.of());
    }

}
