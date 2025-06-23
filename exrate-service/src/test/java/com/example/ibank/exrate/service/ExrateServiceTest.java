package com.example.ibank.exrate.service;

import com.example.ibank.exrate.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.assertj.core.api.Assertions.assertThat;

public class ExrateServiceTest extends IntegrationTest {

    @Value( "${scheduling.enabled:true}")
    Boolean schedulingEnabled;

    @Autowired
    ExrateService srv;

    @Test
    void refreshRates_ok() throws Exception {

        // не вызываем явно - вызов должен произойти за счет настроек шедулера
        if( schedulingEnabled) {
            // подождем чтобы точно выполнилось
            Thread.sleep(3 * 1000);
        }
        // нужно вызывать явно если отключен шедулер настройкой scheduling.enabled=false
        else {
            srv.refreshRates();
        }

        // проверяем успешность последнего обновления
        assertThat( srv.isLastRefreshOk()).as( "Bad execution result").isTrue();
    }

}
