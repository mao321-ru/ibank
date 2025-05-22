-- Актуальная схема БД

-- Валюты (справочник)
create table if not exists currencies(
    currency_code varchar(3) primary key,
    currency_name varchar(100) not null check( trim( currency_name) != '') unique,
    create_time timestamp with time zone default current_timestamp not null
);

-- Пользователи
create table if not exists users(
    user_id bigserial primary key,
    login varchar(128) not null check( trim( login) != '') unique,
    password_hash varchar(128) not null,
    user_name varchar(100) not null check( trim( user_name) != ''),
    birth_date date not null,
    create_time timestamp with time zone default current_timestamp not null
);

-- Счета
create table if not exists accounts(
    account_id bigserial primary key,
    user_id bigint not null references users,
    currency_code varchar(3) not null references currencies,
    amount numeric(38,2) not null check( amount >= 0),
    create_time timestamp with time zone default current_timestamp not null,
    constraint accounts_uk unique ( user_id, currency_code)
);

-- индекс для FK
create index if not exists
    accounts_ix_currency_code
on
    accounts( currency_code)
;
