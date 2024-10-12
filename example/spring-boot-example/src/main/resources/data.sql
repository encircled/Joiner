create table employment
(
    id   bigint not null,
    name varchar(255),
    primary key (id)
);

insert into Employment(id, name)
values (1, 'Test Employment 1');
insert into Employment(id, name)
values (2, 'Test Employment 2');