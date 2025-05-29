package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.*;
import com.example.ibank.accounts.repository.UserRepository;

import io.r2dbc.spi.Parameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.r2dbc.core.Parameter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final R2dbcEntityTemplate etm;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional( readOnly = true)
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
    public Mono<Boolean> deleteUser(String login) {
        final String notFoundMsg = "USER_NOT_FOUND";
        return etm.getDatabaseClient()
            .sql("""
                with
                    validate_errors_q as
                        (
                        select
                            :notFoundMsg as error_message
                        from
                            users u
                        where
                            u.login = :login
                        having
                            count(1) = 0
                        union all
                        select
                            'Balance must be zero for close ' || ac.currency_code || ' account'
                        from
                            accounts ac
                        where
                            ac.user_id = (select u.user_id from users u where u.login = :login)
                            and ac.amount != 0
                        ),
                    user_id_q as
                        -- возвращает user_id только при отсутствии ошибок, чтобы исключить фактическое внесение
                        -- изменений в последующих модифицирующих подзапросах (которые будут выполняться БД)
                        (
                        select
                            u.user_id
                        from
                            users u
                        where
                            u.login = :login
                            and not exists (select null from validate_errors_q)
                        ),
                    delete_accounts_q as
                        (
                        delete from
                            accounts ac
                        where
                            ac.user_id = (select t.user_id from user_id_q t)
                            and ac.amount = 0
                        ),
                    delete_users_q as
                        (
                        delete from
                            users u
                        where
                            u.user_id = (select t.user_id from user_id_q t)
                        )
                select
                    max( e.error_message) as error_message
                from
                    (
                    select
                        ve.*
                    from
                        validate_errors_q ve
                    limit 1
                    ) e
            """)
                .bind( "login", login)
                .bind( "notFoundMsg", notFoundMsg)
                .map( row -> {
                    var errorMessage = row.get( "error_message", String.class);
                    if( errorMessage != null && ! notFoundMsg.equals( errorMessage)) {
                        throw new IllegalStateException( errorMessage);
                    }
                    return errorMessage == null;
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
    @Transactional
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

    @Override
    public Mono<Boolean> updateUserAccounts(String login, UserUpdateRequest rq) {
        log.debug( "updateUserAccounts: userName: {}", rq.getName());
        final String notFoundMsg = "USER_NOT_FOUND";
        return etm.getDatabaseClient()
            .sql("""
                with
                    validate_errors_q as
                        (
                        select
                            :notFoundMsg as error_message
                        from
                            users u
                        where
                            u.login = :login
                        having
                            count(1) = 0
                        union all
                        select
                            'Balance must be zero for close ' || ac.currency_code || ' account'
                        from
                            accounts ac
                        where
                            ac.user_id = (select u.user_id from users u where u.login = :login)
                            and not exists
                                (
                                select
                                    null
                                from
                                    (select unnest( :currencies) currency_code) t
                                where
                                    t.currency_code = ac.currency_code
                                )
                            and ac.amount != 0
                        ),
                    user_id_q as
                        -- возвращает user_id только при отсутствии ошибок, чтобы исключить фактическое внесение
                        -- изменений в последующих модифицирующих подзапросах (которые будут выполняться БД)
                        (
                        select
                            u.user_id
                        from
                            users u
                        where
                            u.login = :login
                            and not exists (select null from validate_errors_q)
                        ),
                    update_users_q as
                        (
                        update
                            users u
                        set
                            user_name = coalesce( nullif( trim( :userName), ''), user_name),
                            birth_date = coalesce( :birthDate, birth_date)
                        where
                            u.user_id = (select t.user_id from user_id_q t)
                        ),
                    merge_accounts_q as
                        (
                        merge into
                            accounts d
                        using
                            (
                            select distinct
                                u.user_id,
                                t.currency_code
                            from
                                (select unnest( :currencies) currency_code) t
                                cross join user_id_q u
                            ) s
                        on
                            d.user_id = s.user_id
                            and d.currency_code = s.currency_code
                        when not matched then
                            insert
                            (
                                user_id,
                                currency_code,
                                amount
                            )
                            values
                            (
                                s.user_id,
                                s.currency_code,
                                0
                            )
                        when not matched by source and
                                d.user_id = (select user_id from user_id_q)
                                -- закрываем только нулевые счета
                                and d.amount = 0
                            then delete
                        )
                select
                    max( e.error_message) as error_message
                from
                    (
                    select
                        ve.*
                    from
                        validate_errors_q ve
                    limit 1
                    ) e
            """)
                .bind( "login", login)
                // для передачи null нужно использовать Parameter, иначе будет ошибка
                .bind( "userName", Parameter.fromOrEmpty( rq.getName(), String.class))
                .bind( "birthDate", Parameter.fromOrEmpty( rq.getBirthDate(), LocalDate.class))
                .bind( "currencies", rq.getCurrencies().toArray( new String[0]))
                .bind( "notFoundMsg", notFoundMsg)
                .map( row -> {
                    var errorMessage = row.get( "error_message", String.class);
                    if( errorMessage != null && ! notFoundMsg.equals( errorMessage)) {
                        throw new IllegalStateException( errorMessage);
                    }
                    return errorMessage == null;
                })
                .one()
        ;
    }

}
