package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.*;
import com.example.ibank.accounts.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final R2dbcEntityTemplate etm;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Mono<UserInfo> createUser( UserCreate rq) {
        log.debug( "createUser: login: {}", rq.getLogin());
        // Регистрирует пользователя если такого логина еще нет
        return etm.getDatabaseClient().sql("""
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
                returning user_id, login, user_name, birth_date
            """)
                        .bind( "login", rq.getLogin())
                        .bind( "password_hash", passwordEncoder.encode( rq.getPassword()))
                        .bind( "user_name", rq.getUserName())
                        .bind( "birth_date", rq.getBirthDate())
                        .map( row -> {
                            log.debug( "inserted: user_id: {}", row.get("user_id", Long.class));
                            return new UserInfo()
                                .login( row.get( "login", String.class))
                                .userName( row.get( "user_name", String.class))
                                .birthDate( row.get( "birth_date", LocalDate.class))
                            ;
                        })
                        .one()
                ;
    }

    @Override
    @Transactional( readOnly = true)
    public Mono<UserInfo> validate( String login, String password) {
        return repo.findByLogin( login)
            .doOnNext( u -> log.trace( "found userId: {}", u.getId()))
            .filter( u -> passwordEncoder.matches( password, u.getPasswordHash()))
            .map( u -> new UserInfo()
                .login( u.getLogin())
                .userName( u.getUserName())
                .birthDate( u.getBirthDate())
            )
            .switchIfEmpty( Mono.error( new IllegalArgumentException( "Invalid username or password")))
        ;
    }

    @Override
    public Mono<Boolean> changePassword(  String login, String password) {
        return etm.getDatabaseClient()
            .sql("""
                update
                    users u
                set
                    password_hash = :passwordHash
                where
                    u.login = :login
                """)
            .bind( "passwordHash", passwordEncoder.encode( password))
            .bind( "login", login)
            .fetch()
            .rowsUpdated()
            .doOnNext( rowCount -> log.debug( "changePassword: updated rows: {}", rowCount))
            .map( rowCount -> rowCount > 0)
       ;
    }

}
