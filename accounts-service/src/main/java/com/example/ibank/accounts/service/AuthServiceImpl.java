package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.RegisterRequest;
import com.example.ibank.accounts.model.RegisterResponse;
import com.example.ibank.accounts.model.ValidateRequest;
import com.example.ibank.accounts.model.AuthResponse;
import com.example.ibank.accounts.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;
    private final R2dbcEntityTemplate etm;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional( readOnly = true)
    public Mono<AuthResponse> validate( ValidateRequest request) {
        log.debug( "validate for: login: {}", request.getLogin());
        return repo.findByLogin( request.getLogin())
            .doOnNext( u -> log.trace( "found userId: {}", u.getId()))
            .filter( u -> passwordEncoder.matches( request.getPassword(), u.getPasswordHash()))
            .map( u -> new AuthResponse()
                .login( u.getLogin())
                .roles( List.of())
            )
            .switchIfEmpty( Mono.error( new IllegalArgumentException( "Invalid username or password")))
        ;
    }

    @Override
    @Transactional
    public Mono<RegisterResponse> register( RegisterRequest rq) {
        log.debug( "register: login: {}", rq.getLogin());
        final DatabaseClient dc = etm.getDatabaseClient();
        return
            // Регистрирует пользователя если такого логина еще нет
            dc.sql("""
                insert into
                    users
                (
                    login,
                    password_hash,
                    user_name,
                    birth_date
                )
                select
                    s.*
                from
                    (
                    select
                        :login as login,
                        :password_hash as password_hash,
                        :user_name as user_name,
                        :birth_date as birth_date
                    ) s
                where
                    not exists
                        (
                        select
                            null
                        from
                            users t
                        where
                            t.login = s.login
                        )
                returning user_id, login
            """)
                .bind( "login", rq.getLogin())
                .bind( "password_hash", passwordEncoder.encode( rq.getPassword()))
                .bind( "user_name", rq.getUserName())
                .bind( "birth_date", rq.getBirthDate())
                .map( row -> {
                    log.debug( "inserted: user_id: {}", row.get("user_id", Long.class));
                    return new RegisterResponse().login( row.get( "login", String.class));
                })
                .one()
            ;
    }

}
