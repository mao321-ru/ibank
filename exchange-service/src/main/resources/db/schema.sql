-- Актуальная схема БД

-- Валюты (справочник)
create table if not exists currencies(
    currency_code varchar(3) primary key,
    currency_name varchar(100) not null check( trim( currency_name) != '') unique,
    create_time timestamp with time zone default current_timestamp not null
);

-- Курсы валют к рублю (RUB)
create table if not exists currency_rates(
    rate_id bigserial primary key,
    currency_code varchar(3) not null references currencies,
    rate numeric(19,6) not null check( rate > 0), -- 1 единица валюты = X RUB
    valid_from timestamp with time zone not null,
    valid_to timestamp with time zone,  -- null означает текущий активный курс
    create_time timestamp with time zone default current_timestamp not null,
    constraint rub_rate check (currency_code != 'RUB' or rate = 1),
    constraint valid_period check (valid_to is null or valid_to >= valid_from)
);

-- индекс для FK
create index if not exists
    currency_rates_ix_currency_code
on
    currency_rates( currency_code)
;

-- индекс для выборки актуальных курсов
create unique index if not exists
    currency_rates_ix_current
on
    currency_rates( currency_code)
where
    valid_to is null
;
