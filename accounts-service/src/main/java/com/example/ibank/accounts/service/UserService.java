package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface UserService {

    Flux<UserShort> getAllUsers();

    Mono<UserInfo> createUser(UserCreate rq);

    Mono<Boolean> deleteUser(String login);

    Mono<UserInfo> validate( String login, String password);

    Mono<Boolean> changePassword( String login, String password);

    Mono<UserAccounts> getUserAccounts( String login);

    Mono<Boolean> updateUserAccounts( String login, UserUpdateRequest rq);
}
