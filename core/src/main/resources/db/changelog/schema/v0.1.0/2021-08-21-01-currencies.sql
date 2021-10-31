--liquibase formatted sql
--changeset skharitonov:2021-08-21-init-currencies-table
create table if not exists currencies
(
    id               bigserial primary key,
    code             varchar(3) not null unique,
    multiplier       numeric    not null,
    creation_date    timestamp  not null,
    last_update_date timestamp  not null,
    version          int        not null
);
--rollback drop table currencies;

--changeset skharitonov:2021-08-21-add-index-to-currencies-code
create index if not exists currencies_code_index on currencies (code);
--rollback drop index currencies_code_index;