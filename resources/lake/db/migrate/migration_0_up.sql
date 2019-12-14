create schema migrations;

create table migrations.migration
(
    idx int not null
);

create unique index migration_idx_uindex
    on migrations.migration (idx);

alter table migrations.migration
    add constraint migration_pk
        primary key (idx);
