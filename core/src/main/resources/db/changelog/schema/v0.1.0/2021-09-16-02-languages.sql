--liquibase formatted sql
--changeset skharitonov:2021-09-16-init-languages-table
create table if not exists languages
(
    id               bigserial primary key,
    code             varchar(3) not null unique,
    creation_date    timestamp  not null,
    last_update_date timestamp  not null,
    version          int        not null
);
--rollback drop table languages;

--changeset skharitonov:2021-09-16-add-index-to-languages-code
create index if not exists languages_code_index on languages (code);
--rollback drop index languages_code_index;