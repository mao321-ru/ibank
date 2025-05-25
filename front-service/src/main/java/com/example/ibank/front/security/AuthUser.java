package com.example.ibank.front.security;

import lombok.Builder;
import lombok.Data;

import java.security.Principal;
import java.time.LocalDate;

@Builder
@Data
public class AuthUser implements Principal {

    private final String login;
    private final String userName;
    private final LocalDate birthDate;

    @Override
    public String getName() {
        return login;
    }
}
