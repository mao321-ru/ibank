-- Актуальная схема БД

-- События для нотификации
create table if not exists events(
    event_id bigserial primary key,
    source varchar(128) not null check( trim( source) != ''),
    event_type varchar(128) check( trim( event_type) != ''),
    event_time timestamp with time zone default current_timestamp not null,
    user_login varchar(128),
    message varchar(1000) not null check( trim( message) != ''),
    create_time timestamp with time zone default current_timestamp not null
);
