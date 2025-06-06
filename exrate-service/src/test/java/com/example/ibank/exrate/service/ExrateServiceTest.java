package com.example.ibank.exrate.service;

import com.example.ibank.exrate.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class ExrateServiceTest extends IntegrationTest {

    @Autowired
    ExrateService srv;

    @Test
    void refreshRates_ok() throws Exception {

        // не вызываем явно - вызов должен произойти за счет настроек шедулера
        // если нужно вызывать явно - нужно отключить шедулер настройкой scheduling.enabled=false
        //srv.refreshRates();

        // посмотреть на процесс...
        Thread.sleep( 10 * 1000);

        // ждем не более 5 секунд появления асинхронного результата
        for( int i = 0; i < 5 && srv.isLastRefreshOk() == null; i++) Thread.sleep( 1 * 1000);

        // проверяем успешность последнего обновления
        assertThat( srv.isLastRefreshOk()).as( "Bad execution result").isTrue();
    }

}
