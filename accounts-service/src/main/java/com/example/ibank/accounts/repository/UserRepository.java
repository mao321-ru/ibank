package com.example.ibank.accounts.repository;

import com.example.ibank.accounts.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<User> findByLogin(String login);
}
