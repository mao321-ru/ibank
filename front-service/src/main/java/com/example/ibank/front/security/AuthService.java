package com.example.ibank.front.security;

import com.example.ibank.front.accounts.model.AuthResponse;
import com.example.ibank.front.accounts.model.RegisterResponse;
import com.example.ibank.front.dto.SignupDto;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface AuthService {

    Mono<AuthResponse> authenticate(String login, String password);

    Mono<RegisterResponse> register(SignupDto sd);

    Mono<Void> changePassword(String login, String password);

    boolean isAdult(LocalDate birthDate);

}