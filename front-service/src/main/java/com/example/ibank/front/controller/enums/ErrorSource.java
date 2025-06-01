package com.example.ibank.front.controller.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorSource {
    DeleteUser ( "deleteUserErrors"),
    Password ( "passwordErrors"),
    UserAccounts ( "userAccountsErrors"),
    CashAction ( "cashErrors");

    private final String paramName;
}
