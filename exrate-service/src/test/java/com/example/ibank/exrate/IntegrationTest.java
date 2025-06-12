package com.example.ibank.exrate;

import com.example.ibank.common.IntegrationTestBase;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
// для явного вызова в тестах отключаем автозапуск обновления курсов по расписанию за счет настройки в SchedulingConfig
// иначе возможно одновременное изменение и ошибка в одном из запусков (по расписанию и в тесте) из-за нарушения UK в БД
//@TestPropertySource( properties = "scheduling.enabled=false")
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( List.of(
            Container.POSTGRES,
            Container.EXCHANGE_SERVICE
        ));
    }

}
