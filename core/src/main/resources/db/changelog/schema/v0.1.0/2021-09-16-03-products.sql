--liquibase formatted sql
--changeset skharitonov:2021-09-16-init-products-table
create table if not exists products
(
    id               bigserial primary key,
    price            numeric   not null,
    creation_date    timestamp not null,
    last_update_date timestamp not null,
    version          int       not null
);
--rollback drop table products;