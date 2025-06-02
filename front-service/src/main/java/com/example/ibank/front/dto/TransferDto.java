package com.example.ibank.front.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.BindParam;

import java.math.BigDecimal;

@Data
public class TransferDto {

    @BindParam( "from_currency")
    private final String fromCurrency;

    @BindParam( "to_currency")
    private final String toCurrency;

    @BindParam( "value")
    private final BigDecimal amount;

    @BindParam( "to_login")
    private final String toLogin;
}

