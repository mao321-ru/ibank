delete from currency_rates where currency_code != 'RUB';

alter sequence currency_rates_rate_id_seq restart with 10;

insert into
    currency_rates
(
    currency_code,
    rate,
    valid_from
)
select
    s.*
from
    (
    select
        'USD' as currency_code,
        50 as rate,
        current_timestamp as valid_from
    union all
    select
        'EUR' as currency_code,
        55 as rate,
        current_timestamp as valid_from
    ) s
where
    not exists
        (
        select
            null
        from
            currency_rates t
        where
            t.currency_code = s.currency_code
        )
;


-- id для временных данных (создавемые в процессе тестов) начинаются с 1001
alter sequence currency_rates_rate_id_seq restart with 1001;
