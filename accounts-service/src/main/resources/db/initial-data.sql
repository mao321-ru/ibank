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
