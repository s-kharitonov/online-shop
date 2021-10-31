--liquibase formatted sql
--changeset skharitonov:2021-08-21-init-currencies-01
insert into currencies (code, multiplier, creation_date, last_update_date, version)
values ('RUB', 1, current_timestamp, current_timestamp, 0);
--rollback delete from currencies c where c.code = 'RUB';

--changeset skharitonov:2021-08-21-init-currencies-02
insert into currencies (code, multiplier, creation_date, last_update_date, version)
values ('USD', 74, current_timestamp, current_timestamp, 0);
--rollback delete from currencies c where c.code = 'USD';

--changeset skharitonov:2021-08-21-init-currencies-03
insert into currencies (code, multiplier, creation_date, last_update_date, version)
values ('GBP', 100, current_timestamp, current_timestamp, 0);
--rollback delete from currencies c where c.code = 'GBP';