[![Build Status](https://travis-ci.org/encircled/Joiner.svg?branch=master)](https://travis-ci.org/encircled/Joiner)
[![codecov](https://codecov.io/gh/encircled/Joiner/branch/master/graph/badge.svg)](https://codecov.io/gh/encircled/Joiner)

# Overview

Joiner is a Java library which allows to create type-safe JPA queries. It is focused on applications with complex domain model, which require a lot of work with query joins.   

Joiner can be used instead of or together with QueryDSL. Joiner uses QueryDSL APT maven plugin for entitiy metamodel generation. See more about QueryDSL installation at [QueryDSL](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration).

Joiner offers following extra features:
* simple way of adding complex joins to the queries
* automatic resolving of alias uniqueness in queries
* fixed join fetching in Eclipselink (when using inheritance)

# Example setup

## Non-spring environment
All you need is entity manager instance, setup of Joiner itself is as simple as:

```java
Joiner joiner = new Joiner(getEntityManager());
joiner.find(Q.from(QUser.user)
                .where(QUser.user.isNotNull()));
```

# Features

## Nested joins

Joiner represents query joins as a graph, which allows to automatically resolve unique aliases for nested joins (even when there are name collisions in different tree branches).

Aliases of nested joins are determined at runtime. To refer an unambiguous nested join, you can just use it's alias, otherwise use `J.path(...)` util method.     
For example, there is a query like:

```java
Q.from(QGroup.group)
    .joins(J.left(QPerson.person)
                .nested(QContact.contact))
```

`Person` is not a nested join and should be referenced directly:

```java
Q.from(QGroup.group)
    .joins(J.left(QPerson.person)
                .nested(J.left(QContact.contact)))
    .where(QPerson.person.name.eq('Chuck'));
```

To reference a `Contact` entity (in `where` clause etc), `QContact.contact` can be used directly as well:

```java
Q.from(QGroup.group)
    .joins(J.left(QPerson.person)
            .nested(QContact.contact))
    .where(QContact.contact.number.eq(12345));
```

However, in cases when there are multiple alias candidates, `J.path(...)`. For example to predicate a contact of second person:

```java
Q.from(QGroup.group)
        .joins(J.left(QPerson.person)
                        .nested(QContact.contact),
                J.left(new QPerson("secondPerson"))
                        .nested(QContact.contact))
        .where(J.path(new QPerson("secondPerson"), QContact.contact).attribute.eq("secondContactAttribute"))
```

## Examples

### Query joins

Basic select query
```java
joiner.findOne(Q.from(QGroup.group)
                .where(QGroup.group.id.eq(1L)));
```

Basic join
```java
joiner.findOne(Q.from(QGroup.group)
                .joins(J.left(QUser.user1))
                .where(QGroup.group.id.eq(1L)));
```

Joining a subclass only (`SuperUser` extends `User`)
```java
joiner.findOne(Q.from(QGroup.group)
                .joins(QSuperUser.superUser)
                .where(QGroup.group.id.eq(1L)));
```

Joining an association, which is present on a subclass only (`Key` is present on `SuperUser` only)
```java
joiner.findOne(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                        .nested(QKey.key))
                .where(QGroup.group.id.eq(1L)));
```

Joining multiple nested associations
```java
joiner.findOne(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                        .nested(QKey.key,QContact.contact))
                .where(QGroup.group.id.eq(1L)));
```
### Result projection
By default, list or single result of `from` clause is returned (for `find` and `findOne` respectively).   
Next example shows how to return another projection:   
```java
joiner.find(Q.select(QPhone.phone.number)
                .from(QUser.user)
                .joins(QPhone.phone));
```

`Q.select` can be used for tuples as well:

```java
joiner.find(Q.select(QUser.user.id, QPhone.phone.number)
                .from(QUser.user)
                .joins(QPhone.phone));
```

## Maven dependencies  

### Core module
```xml
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-core</artifactId>
    <version>${joiner.version}</version>
</dependency>
```

### Spring integration support module
```xml
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-spring</artifactId>
    <version>${joiner.version}</version>
</dependency>
```

### Eclipselink support module
```xml
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-eclipse</artifactId>
    <version>${joiner.version}</version>
</depend
