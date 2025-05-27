create table customer
(
    id   bigint not null,
    name varchar(255),
    primary key (id)
);

create table employment
(
    id   bigint not null,
    customer_id   bigint not null,
    name varchar(255),
    primary key (id)
);


insert into Customer(id, name)
values (1, 'Test Customer 1');

insert into Customer(id, name)
values (2, 'Test Customer 2');

insert into Employment(id, name, customer_id)
values (1, 'Test Employment 1', 1);
insert into Employment(id, name, customer_id)
values (2, 'Test Employment 2', 1);