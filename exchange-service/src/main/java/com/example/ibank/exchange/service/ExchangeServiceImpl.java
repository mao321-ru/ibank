package com.example.ibank.exchange.service;


import com.example.ibank.exchange.model.CurrentRate;
import com.example.ibank.exchange.model.ExchangeRequest;
import com.example.ibank.exchange.model.ExchangeResponse;
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

}
