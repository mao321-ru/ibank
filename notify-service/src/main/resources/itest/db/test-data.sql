delete from events;

-- id для временных данных (создавемые в процессе тестов) начинаются с 1001
alter sequence events_event_id_seq restart with 1001;
