package com.example.ibank.exchange.service;


import com.example.ibank.exchange.model.CurrentRate;
import com.example.ibank.exchange.model.ExchangeRequest;
import com.example.ibank.exchange.model.ExchangeResponse;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
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
public class ExchangeServiceImpl implements ExchangeService {

    private final R2dbcEntityTemplate etm;

    @Override
    @NewSpan( "db")
    @Transactional( readOnly = true)
    public Mono<ExchangeResponse> exchange(ExchangeRequest rq) {
        return etm.getDatabaseClient()
            .sql(
                """
                select
                    a.*,
                    round( :amount * a.rate / a.to_rate, 2) as to_amount
                from
                    (
                    select
                        max( case when cr.currency_code = :currency then cr.rate end) as rate,
                        max( case when cr.currency_code = :toCurrency then cr.rate end) as to_rate
                    from
                        currency_rates cr
                    where
                        cr.valid_to is null
                        and cr.currency_code in ( :currency, :toCurrency)
                    ) a
                """
            )
                .bind( "amount", rq.getAmount())
                .bind( "currency", rq.getCurrency())
                .bind( "toCurrency", rq.getToCurrency())
                .map( row -> {
                    BigDecimal amount = row.get( "to_amount", BigDecimal.class);
                    if( amount == null) {
                        throw new IllegalStateException(
                            "Нет текущего курса валюты [%s]".formatted(
                                row.get( "rate", BigDecimal.class) == null ? rq.getCurrency() :
                                row.get( "to_rate", BigDecimal.class) == null ? rq.getToCurrency() :
                                "?"
                            )
                        );
                    }
                    return new ExchangeResponse().amount( amount).currency( rq.getToCurrency());
                })
                .one()
        ;
    }

    @Override
    @NewSpan( "db")
    @Transactional( readOnly = true)
    public Flux<CurrentRate> getRates() {
        return etm.getDatabaseClient()
            .sql(
                """
                select
                    cr.currency_code,
                    c.currency_name,
                    cr.rate
                from
                    currency_rates cr
                    join currencies c
                        on c.currency_code = cr.currency_code
                where
                    cr.valid_to is null
                    and cr.currency_code != 'RUB'
                order by
                    c.currency_name
                """
            )
            .map( row -> new CurrentRate()
                .currencyCode( row.get( "currency_code", String.class))
                .currencyName( row.get( "currency_name", String.class))
                .rate( row.get( "rate", BigDecimal.class))
            )
            .all()
        ;
    }

    @Override
    @NewSpan( "db")
    @Transactional
    public Mono<Void> setRates(List<RateShort> rq) {
        return etm.getDatabaseClient()
            .sql(
                """
                with
                    param_q as
                        (
                        select
                            unnest( :currency_codes) currency_code,
                            unnest( :rates) rate
                        ),
                    validate_errors_q as
                        (
                        select
                            'Unknown currency code [' || p.currency_code || ']' as error_message
                        from
                            param_q p
                            left join currencies cr
                                on cr.currency_code = p.currency_code
                        where
                            cr.currency_code is null
                        order by
                            p.currency_code
                        fetch first 1 row only
                        ),
                    rates_q as
                        (
                        select
                            p.*,
                            current_timestamp as valid_from,
                            rt.rate_id
                        from
                            param_q p
                            left join currency_rates rt
                                on rt.currency_code = p.currency_code
                                    and rt.valid_to is null
                        where
                            -- только при отсутствии ошибок
                            not exists (select null from validate_errors_q)
                            -- игнорируем если курс не изменился
                            and rt.rate is distinct from p.rate
                        ),
                    merge_rates_q as
                        (
                        merge into
                            currency_rates d
                        using
                            (
                            -- записи для update перед insert чтобы не нарушить уникальность
                            select
                                t.currency_code,
                                t.rate,
                                t.valid_from,
                                t.rate_id
                            from
                                rates_q t
                            where
                                t.rate_id is not null
                            union all
                            select
                                t.currency_code,
                                t.rate,
                                t.valid_from,
                                null as rate_id
                            from
                                rates_q t
                            ) s
                        on
                            d.rate_id = s.rate_id
                        when matched then
                            update set
                                valid_to = s.valid_from
                        when not matched then
                            insert
                            (
                                currency_code,
                                rate,
                                valid_from
                            )
                            values
                            (
                                s.currency_code,
                                s.rate,
                                s.valid_from
                            )
                        )
                select
                    max( e.error_message) as error_message
                from
                    validate_errors_q e
                """
            )
                .bind( "currency_codes", rq.stream().map( RateShort::currencyCode).toArray( String[]::new))
                .bind( "rates", rq.stream().map( RateShort::rate).toArray( BigDecimal[]::new))
                .map( row -> {
                    var errorMessage = row.get( "error_message", String.class);
                    if( errorMessage != null) throw new IllegalStateException( errorMessage);
                    return true;
                })
            .one()
            .then()
       ;
    }

}
