# Overview

Joiner is a Java library which allows to create type-safe JPA queries. It is focused on applications with complex domain model, which require a lot of work with query joins.   

Joiner can be used instead of or together with QueryDSL. Joiner uses QueryDSL APT maven plugin for entitiy metamodel generation. See more about QueryDSL installation at [QueryDSL](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration).

Joiner offers following extra features:
* simple way of adding complex joins to the queries
* automatic resolving of alias uniqueness in queries
* fixed join fetching in Eclipselink (when using inheritance)

Joiner represents query joins as a graph, which makes it possible to automatically resolve unique aliases for nested joins (even when there are name collisions).

## Examples

Basic select query
```
joiner.findOne(Q.from(QGroup.group)
                .where(QGroup.group.id.eq(1L)));
```

Basic join
```
joiner.findOne(Q.from(QGroup.group)
                .joins(J.left(QUser.user1))
                .where(QGroup.group.id.eq(1L)));
```

Joining a subclass only (`SuperUser` extends `User`)
```
joiner.findOne(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser))
                .where(QGroup.group.id.eq(1L)));
```

Joining an association, which is present on a subclass only (`Key` is present on `SuperUser` only)
```
joiner.findOne(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                        .nested(J.left(QKey.key)))
                .where(QGroup.group.id.eq(1L)));
```

Joining multiple nested associations
```
joiner.findOne(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                        .nested(
                                J.left(QKey.key),
                                J.left(QContact.contact)
                        ))
                .where(QGroup.group.id.eq(1L)));
```

## Maven dependencies  

### Core module
```
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-core</artifactId>
    <version>${joiner.version}</version>
</dependency>
```

### Spring integration support module
```
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-spring</artifactId>
    <version>${joiner.version}</version>
</dependency>
```

### Eclipselink support module
```
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-eclipse</artifactId>
    <version>${joiner.version}</version>
</depend
