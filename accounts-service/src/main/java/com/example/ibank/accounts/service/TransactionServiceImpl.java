package com.example.ibank.accounts.service;

import com.example.ibank.accounts.model.CashTransactionRequest;
import com.example.ibank.accounts.model.TransferTransactionRequest;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final R2dbcEntityTemplate etm;

    @Override
    @NewSpan( "db")
    @Transactional
    public Mono<Void> createCashTransaction(CashTransactionRequest rq) {
        return etm.getDatabaseClient()
            .sql("""
                with
                    validate_errors_q as
                        (
                        select
                            'User with login [' || :login || '] not found' as error_message
                        from
                            users u
                        where
                            u.login = :login
                        having
                            count(1) = 0
                        union all
                        select
                            case when max( ac.currency_code) is null then
                                'Нужно открыть счет в [' || :currency || '] для выполнения операции'
                            else
                                'Недостаточно средств на счете [' || max( ac.currency_code) || '] для выполнения операции'
                            end
                        from
                            accounts ac
                        where
                            ac.user_id = (select u.user_id from users u where u.login = :login)
                            and ac.currency_code = :currency
                        having
                            count(1) = 0
                            or max( ac.amount) + :operationAmount < 0
                        ),
                    account_id_q as
                        -- возвращает account_id только при отсутствии ошибок, чтобы исключить фактическое внесение
                        -- изменений в последующих модифицирующих подзапросах (которые будут выполняться БД)
                        (
                        select
                            ac.account_id
                        from
                            accounts ac
                        where
                            ac.user_id = (select u.user_id from users u where u.login = :login)
                            and ac.currency_code = :currency
                            and not exists (select null from validate_errors_q)
                        ),
                    update_accounts_q as
                        (
                        update
                            accounts ac
                        set
                            amount = ac.amount + :operationAmount
                        where
                            ac.account_id = (select t.account_id from account_id_q t)
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
                .bind( "login", rq.getLogin())
                .bind( "currency", rq.getCurrency())
                .bind( "operationAmount", rq.getAmount())
                .map( row -> {
                    var errorMessage = row.get( "error_message", String.class);
                    if( errorMessage != null) {
                        throw new IllegalStateException( errorMessage);
                    }
                    return true;
                })
                .one()
                .then()
        ;
    }

    @Override
    @NewSpan( "db")
    @Transactional
    public Mono<Void> createTransferTransaction(TransferTransactionRequest rq) {
        return etm.getDatabaseClient()
                .sql("""
                with
                    validate_errors_q as
                        (
                        select
                            'User with login [' || :login || '] not found' as error_message
                        from
                            users u
                        where
                            u.login = :login
                        having
                            count(1) = 0
                        union all
                        select
                            case when max( ac.currency_code) is null then
                                'Нужно открыть счет в [' || :currency || '] для выполнения операции'
                            else
                                'Недостаточно средств на счете [' || max( ac.currency_code) || '] для выполнения операции'
                            end
                        from
                            accounts ac
                        where
                            ac.user_id = (select u.user_id from users u where u.login = :login)
                            and ac.currency_code = :currency
                        having
                            count(1) = 0
                            or max( ac.amount) - :operationAmount < 0
                        union all
                        select
                            'Получатель с логином [' || :toLogin || '] не найден' as error_message
                        from
                            users u
                        where
                            u.login = :toLogin
                        having
                            count(1) = 0
                        union all
                        select
                            'Получателю нужно открыть счет в [' || :toCurrency || '] для выполнения операции'
                        from
                            accounts ac
                        where
                            ac.user_id = (select u.user_id from users u where u.login = :toLogin)
                            and ac.currency_code = :toCurrency
                        having
                            count(1) = 0
                        ),
                    account_id_q as
                        -- возвращает account_id только при отсутствии ошибок, чтобы исключить фактическое внесение
                        -- изменений в последующих модифицирующих подзапросах (которые будут выполняться БД)
                        (
                        select
                            ac.account_id
                        from
                            accounts ac
                        where
                            ac.user_id = (select u.user_id from users u where u.login = :login)
                            and ac.currency_code = :currency
                            and not exists (select null from validate_errors_q)
                        ),
                    to_account_id_q as
                        (
                        select
                            ac.account_id
                        from
                            accounts ac
                        where
                            ac.user_id = (select u.user_id from users u where u.login = :toLogin)
                            and ac.currency_code = :toCurrency
                            and not exists (select null from validate_errors_q)
                        ),
                    update_accounts_q as
                        (
                        update
                            accounts ac
                        set
                            amount = ac.amount - :operationAmount
                        where
                            ac.account_id = (select t.account_id from account_id_q t)
                        ),
                    update_to_accounts_q as
                        (
                        update
                            accounts ac
                        set
                            amount = ac.amount + :toOperationAmount
                        where
                            ac.account_id = (select t.account_id from to_account_id_q t)
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
                .bind( "login", rq.getLogin())
                .bind( "currency", rq.getCurrency())
                .bind( "operationAmount", rq.getAmount())
                .bind( "toLogin", rq.getToLogin())
                .bind( "toCurrency", rq.getToCurrency())
                .bind( "toOperationAmount", rq.getToAmount())
                .map( row -> {
                    var errorMessage = row.get( "error_message", String.class);
                    if( errorMessage != null) {
                        throw new IllegalStateException( errorMessage);
                    }
                    return true;
                })
                .one()
                .then()
                ;
    }
}
