package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.*;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<UserInfo> createUser(UserCreate request);

    Mono<UserInfo> validate( String login, String password);

    Mono<Boolean> changePassword( String login, String password);

}
