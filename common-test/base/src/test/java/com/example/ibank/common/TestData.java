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

    // Можно редактировать имя, дату рождения и счета
    String EDITED_USER_LOGIN = "editedUser";
    String EDITED_USER_PASSWORD = EXISTS_USER_PASSWORD;

    // Можно удалить (нет ненулевых счетов)
    String DELETED_USER_LOGIN = "deletedUser";
    String DELETED_USER_PASSWORD = EXISTS_USER_PASSWORD;

    // Тестирование операций с наличными (открыты нулевые счета RUB и USD)
    String CASH_USER_LOGIN = "cashUser";
    String CASH_USER_PASSWORD = EDITED_USER_PASSWORD;
    String CASH_USER_RUB_AMOUNT = "0";
    String CASH_USER_USD_AMOUNT = "200.01";

    // Тестирование переводов (открыты счета 1000 RUB и 0 USD)
    String TRANSFER_USER_LOGIN = "transferUser";
    String TRANSFER_USER_PASSWORD = EDITED_USER_PASSWORD;
    String TRANSFER2_USER_LOGIN = "transferUser2";
    String TRANSFER2_USER_PASSWORD = EDITED_USER_PASSWORD;

    // Тестирование переводов (открыты нулевые счета RUB и EUR)
    String TO_TRANSFER_USER_LOGIN = "toTransferUser";
    String TO_TRANSFER_USER_PASSWORD = EDITED_USER_PASSWORD;

    // Число валют
    int CURRENCIES_COUNT = 3;
    String CURRENCY_RUB_CODE = "RUB";
    String CURRENCY_RUB_NAME = "Российский рубль";

}
