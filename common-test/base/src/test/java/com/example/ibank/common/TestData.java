package com.example.ibank.common;

// Общие тестовые данные, используемые в модулях
interface TestData {

    // Логин и пароль существующего пользователя
    String EXISTS_USER_LOGIN = "ivan";
    String EXISTS_USER_PASSWORD = "user";
    String EXISTS_USER_NAME = "Петров Иван";
    String EXISTS_USER_BIRTHDATE = "1979-03-01";
    String EXISTS_USER_RUB_AMOUNT = "1000.01";

    // Существующий пользователь (можно менять пароль)
    String EXISTS_USER2_LOGIN = "user2";

    // Число валют
    int CURRENCIES_COUNT = 3;
    String CURRENCY_RUB_CODE = "RUB";
    String CURRENCY_RUB_NAME = "Российский рубль";

}
