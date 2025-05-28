package com.example.ibank.front.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDate;
import java.util.List;

@Data
public class EditUserAccountsDto {

    private final String name;

    @BindParam( "birthdate")
    private final LocalDate birthDate;

    @BindParam( "account")
    private final List<String> accountCurrencies;
}

