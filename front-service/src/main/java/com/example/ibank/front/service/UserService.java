package com.example.ibank.front.service;

import com.example.ibank.front.accounts.model.UserInfo;
import com.example.ibank.front.accounts.model.UserShort;
import com.example.ibank.front.dto.SignupDto;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface UserService {

    Mono<UserInfo> authenticate(String login, String password);

    Mono<UserInfo> register(SignupDto sd);

    Mono<Void> changePassword(String login, String password);

    Mono<List<UserShort>> getUsers();

    boolean isAdult(LocalDate birthDate);

}