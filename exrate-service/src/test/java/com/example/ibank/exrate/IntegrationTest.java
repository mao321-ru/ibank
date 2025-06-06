package com.example.ibank.exrate;

import com.example.ibank.common.IntegrationTestBase;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

// Общие настройки для всех интеграционных тестов модуля
@TestPropertySource( properties = "spring.config.import=configserver:http://localhost:8926")
// для явного вызова в тестах отключаем автозапуск обновления курсов по расписанию за счет настройки в SchedulingConfig
// иначе возможно одновременное изменение и ошибка в одном из запусков (по расписанию и в тесте) из-за нарушения UK в БД
//@TestPropertySource( properties = "scheduling.enabled=false")
public abstract class IntegrationTest extends IntegrationTestBase {

    static {
        startContainers( 8926, List.of(
            Container.EUREKA,
            Container.GATEWAY,
            Container.POSTGRES,
            Container.EXCHANGE_SERVICE
        ));
    }

}
