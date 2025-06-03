insert into
    currencies
(
    currency_code,
    currency_name
)
select
    s.*
from
    (
    select
        'RUB' as currency_code,
        'Российский рубль' as currency_name
    union all select 'EUR', 'Евро'
    union all select 'USD', 'Доллар США'
    ) s
where
    s.currency_code not in
        (
        select
            t.currency_code
        from
            currencies t
        )
;


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
        'RUB' as currency_code,
        1 as rate,
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
