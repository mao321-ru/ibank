package com.example.ibank.exrate.service;

public interface ExrateService {

    void refreshRates();

    // для проверки результата refreshRates в тесте
    Boolean isLastRefreshOk();

}
