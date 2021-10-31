--liquibase formatted sql
--changeset skharitonov:2021-09-16-init-languages-01
insert into languages (code, creation_date, last_update_date, version)
values ('RU', current_timestamp, current_timestamp, 0);
--rollback delete from languages l where l.code = 'RU';

--changeset skharitonov:2021-09-16-init-languages-02
insert into languages (code, creation_date, last_update_date, version)
values ('EN', current_timestamp, current_timestamp, 0);
--rollback delete from languages l where l.code = 'EN';