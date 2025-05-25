package com.example.ibank.front.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.BindParam;

@Data
public class EditPasswordDto {

    private final String password;

    @BindParam( "confirm_password")
    private final String confirmPassword;
}

