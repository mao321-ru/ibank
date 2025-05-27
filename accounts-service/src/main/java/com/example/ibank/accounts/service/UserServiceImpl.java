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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
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
    public Flux<UserShort> getAllUsers() {
        return etm.getDatabaseClient().sql(
                """
                select
                    u.login,
                    u.user_name
                from
                    users u
                """
            )
            .map( row -> new UserShort()
                .login( row.get( "login", String.class))
                .name( row.get( "user_name", String.class))
            )
            .all()
        ;
    }

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
                        .bind( "user_name", rq.getName())
                        .bind( "birth_date", rq.getBirthDate())
                        .map( row -> {
                            log.debug( "inserted: user_id: {}", row.get("user_id", Long.class));
                            return new UserInfo()
                                .login( row.get( "login", String.class))
                                .name( row.get( "user_name", String.class))
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
                .name( u.getUserName())
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

    @Override
    public Mono<UserAccounts> getUserAccounts(String login) {
        return etm.getDatabaseClient()
            .sql(
                """
                select
                    u.login,
                    u.user_name,
                    u.birth_date,
                    cr.currency_code,
                    cr.currency_name,
                    coalesce( ac.amount, 0) as amount,
                    ac.account_id is not null as account_exists
                from
                    users u
                    cross join currencies cr
                    left join accounts ac
                        on ac.user_id = u.user_id
                            and ac.currency_code = cr.currency_code
                where
                    u.login = :login
                order by
                    case when cr.currency_code != 'RUB' then cr.currency_name end nulls first
                """
            )
            .bind( "login", login)
            .map( row -> new UserAccounts()
                    .login( row.get( "login", String.class))
                    .name( row.get( "user_name", String.class))
                    .birthDate( row.get( "birth_date", LocalDate.class))
                    .accounts( List.of( new Account()
                        .currency( new Currency()
                            .code( row.get( "currency_code", String.class))
                            .name( row.get( "currency_name", String.class))
                        )
                        .value( row.get( "amount", BigDecimal.class))
                        .exists( row.get( "account_exists", Boolean.class))
                    ))
            )
            .all()
            .collectList()
            .filter( list -> ! list.isEmpty())
            .map( list -> {
                var f = list.getFirst();
                return new UserAccounts()
                    .login( f.getLogin())
                    .name( f.getName())
                    .birthDate( f.getBirthDate())
                    .accounts( list.stream().map( ua -> ua.getAccounts().getFirst()).toList())
                ;
            })
        ;
    }

}
