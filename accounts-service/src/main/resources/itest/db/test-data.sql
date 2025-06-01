delete from accounts;
delete from users;

alter sequence users_user_id_seq restart with 1;
alter sequence accounts_account_id_seq restart with 1;

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
        'ivan' as login,
        -- хэш пароля "user"  (хэш BCryptPasswordEncoder::encode( ...))
        '$2a$10$fkvOpBEpZEUj5qA/xWsSSeVFwjnBBKOqUIGTMmquMtD9rsLAu5DN2' as password_hash,
        'Петров Иван' as user_name,
        DATE '1979-03-01' as birth_date
    union all select
        'user2',
        '$2a$10$fkvOpBEpZEUj5qA/xWsSSeVFwjnBBKOqUIGTMmquMtD9rsLAu5DN2',
        'Меняй Пароль',
        DATE '1980-04-21'
    union all select
        'editedUser',
        '$2a$10$fkvOpBEpZEUj5qA/xWsSSeVFwjnBBKOqUIGTMmquMtD9rsLAu5DN2',
        'Редактируемый Юзер',
        DATE '1979-05-18'
    union all select
        'deletedUser',
        '$2a$10$fkvOpBEpZEUj5qA/xWsSSeVFwjnBBKOqUIGTMmquMtD9rsLAu5DN2',
        'Удаляемый Юзер',
        DATE '1979-05-18'
    union all select
        'cashUser',
        '$2a$10$fkvOpBEpZEUj5qA/xWsSSeVFwjnBBKOqUIGTMmquMtD9rsLAu5DN2',
        'Любитель Кэша',
        DATE '1988-03-11'
    ) s
;

insert into
    accounts
(
    user_id,
    currency_code,
    amount
)
select
    (select u.user_id from users u where u.login = s.login) as user_id,
    s.currency_code,
    s.amount
from
    (
    select 'ivan' as login, 'RUB' as currency_code, 1000.01 as amount
    union all select 'ivan', 'USD', 55.99
    union all select 'deletedUser', 'USD', 0.0
    union all select 'deletedUser', 'EUR', 0.0
    union all select 'cashUser', 'RUB', 0.0
    union all select 'cashUser', 'USD', 0.0
    ) s
;


-- id для временных данных (создавемые в процессе тестов) начинаются с 1001
alter sequence users_user_id_seq restart with 1001;
alter sequence accounts_account_id_seq restart with 1001;
