package com.example.ibank.blocker.controller;

import com.example.ibank.blocker.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;


// Общие настройки и т.д. для интеграционныйх тестов контроллеров
public class ControllerTest extends IntegrationTest {

    @Autowired
    WebTestClient wtc;

}
