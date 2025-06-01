package com.example.ibank.front.dto;

import com.example.ibank.front.controller.enums.CashAction;
import lombok.Data;
import org.springframework.web.bind.annotation.BindParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CashOperationDto {

    private final String currency;

    @BindParam( "value")
    private final BigDecimal amount;

    @BindParam( "action")
    private final CashAction action;
}

