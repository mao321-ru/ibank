package com.example.ibank.notify.controller;

import com.example.ibank.notify.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;


// Общие настройки и т.д. для интеграционныйх тестов контроллеров
public class ControllerTest extends IntegrationTest {

    @Autowired
    WebTestClient wtc;

}
