--liquibase formatted sql
--changeset skharitonov:2021-09-16-init-product-descriptions-table
create table if not exists product_descriptions
(
    id               bigserial primary key,
    product_id       bigint        not null,
    language_id      bigint        not null,
    title            varchar(250)  not null,
    description      varchar(2500) not null,
    creation_date    timestamp     not null,
    last_update_date timestamp     not null,
    version          int           not null,
    constraint fk_product_descriptions_product foreign key (product_id) references products (id) on delete cascade,
    constraint fk_product_descriptions_language foreign key (language_id) references languages (id) on delete cascade
);
--rollback drop table product_descriptions;