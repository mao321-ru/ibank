package com.example.ibank.front.security;

import com.example.ibank.front.accounts.model.UserInfo;
import com.example.ibank.front.accounts.model.UserInfo;
import com.example.ibank.front.dto.SignupDto;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface AuthService {

    Mono<UserInfo> authenticate(String login, String password);

    Mono<UserInfo> register(SignupDto sd);

    Mono<Void> changePassword(String login, String password);

    boolean isAdult(LocalDate birthDate);

}