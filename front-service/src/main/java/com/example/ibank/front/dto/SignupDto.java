package com.example.ibank.front.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDate;

@Data
public class SignupDto {

    private final String login;
    private final String password;

    @BindParam( "confirm_password")
    private final String confirmPassword;

    private final String name;
    private final LocalDate birthdate;
}

