create table author
(
    id        bigint not null,
    status_id bigint not null,
    name      varchar(255),
    primary key (id)
);

create table post
(
    id        bigint not null,
    status_id bigint not null,
    author_id bigint not null,
    name      varchar(255),
    primary key (id)
);

create table status
(
    id   bigint not null,
    name varchar(255),
    primary key (id)
);

insert into status(id, name)
values (1, 'Active Status');

insert into status(id, name)
values (2, 'Draft Status');

insert into author(id, status_id, name)
values (1, 1, 'Author 1');
insert into author(id, status_id, name)
values (2, 2, 'Author 2');

insert into post(id, status_id, author_id, name)
values (1, 1, 1, 'Post of Author 1');
insert into post(id, status_id, author_id, name)
values (2, 2, 1, 'Post of Author 2');
