TODO
======

- Использовать инструмент миграции для БД

В [intershop v1.0](https://github.com/mao321-ru/intershop/releases/tag/v1.0) использовался Liquibase, однако от него пришлость отказаться после перехода r2dbc. В [R2DBC and liquibase](https://stackoverflow.com/questions/62555217/r2dbc-and-liquibase) предлагается использовать отдельный jdbc-драйвер специально для Liquibase. Такой вариант решения кажется нецелесообразным, думаю имеет смысл попробовать другую утилиту для миграции, например Flyway.
