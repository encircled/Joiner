[![Build Status](https://travis-ci.org/encircled/Joiner.svg?branch=master)](https://travis-ci.org/encircled/Joiner)
[![codecov](https://codecov.io/gh/encircled/Joiner/branch/master/graph/badge.svg)](https://codecov.io/gh/encircled/Joiner)

# Overview

Joiner is a Java library which allows creating type-safe JPA queries. It is focused on applications with complex domain model, which require a lot of work with query joins.

Joiner can be used instead of or together with QueryDSL. Joiner uses QueryDSL APT maven plugin for entity metamodel
generation. See more about QueryDSL installation
at [QueryDSL](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration).

Joiner offers Java, Kotlin and reactive API, which are described below

Joiner offers following extra features:

* simple way of adding complex joins to the queries
* automatic resolving of alias uniqueness in queries
* fixed join fetching in Eclipselink (when using inheritance)

### Readme

- [TL;DR](#tldr)
- [Basic query](#basic-query)
- [Basic joins](#basic-join)
- [Customizing a join](#customizing-a-join)
- [Nested joins](#nested-joins)
- [Entity inheritance](#inheritance)
- [Result projection](#result-projection)
- [Sorting](#sorting)
- [Query features](#query-features)
- [Kotlin API showcase](#kotlin-api-showcase)
- [Reactive API](#reactive-api)
- [Example setup](#example-setup)
- [Maven dependencies](#maven-dependencies)

# TL;DR

Ultimately, all the queries are type-safe, support auto-completion and look like:

- Kotlin version
```kotlin
val names = joiner.find(user.name from user
                            innerJoin group
                            leftJoin status
                 
                            where { status.type eq "active" or group.name eq "superUsers" }
                 
                            asc group.name
                            limit 5
                      )
```

- Java version
```java
List<String> names = joiner.find(Q.select(user.name).from(user)
                                          .joins(J.inner(group))
                                          .joins(status)
                                          .where(status.type.eq("active").or(group.name.eq("superUsers")))
                                          .asc(group.name)
                                          .limit(5)
                                  )
```

- Project Reactor Kotlin version
```kotlin
val names : Flux<String> = joiner.find(user.name from user
                                                innerJoin group
                                                leftJoin status
                                     
                                                where { status.type eq "active" or group.name eq "superUsers" }
                                     
                                                asc group.name
                                                limit 5
                                        )
                                  .filter { name -> /* whatever */ }
                                  .flatMap { name -> /* async whatever returning Mono */ }
```

- Kotlin coroutines version
```kotlin
val names = runBlocking {
      joiner.find(user.name from user ...)
}
```

# Features

## Basic query

```java
QGroup group = QGroup.group;

joiner.find(Q.select(group.name).from(group)
                .where(group.id.eq(1L))
                .groupBy(group.type)
                .limit(10)
                .offset(2)
                .distinct());
```

or in Kotlin

```kotlin
joiner.find(group.name from group
                          where { it.id eq 1 }
                          groupBy { it.type }
                          limit 10
                          offset 2
)
```

## Basic join

Example below shows how to join users of group. Target attribute is looked up by type and field name, so it does not
matter which relationship it is:

```java
joiner.findOne(Q.from(QGroup.group)
                  .joins(QUser.user);
```

Aliases can be imported or extracted as a variable to make it:

```java
joiner.findOne(Q.from(group).joins(user));
```

By default, all joins are left fetch joins.

If there are multiple field with the same type, then the name must be specified explicitly. So in case when there
are `user1` and `user2` field on the group, correct way would be:

```java
joiner.findOne(Q.from(group).joins(group.user1));
```

or

```java
joiner.findOne(Q.from(group).joins(new QUser("user2")));
```

in Kotlin

```kotlin
joiner.findOne(group.all() leftJoin group.users)
```

## Customizing a join

To perform an inner join, or to make a non-fetch join (thus it will not be part of the result set)

```java
joiner.findOne(Q.from(group)
                    .joins(J.inner(user).on(user.name.isNotNull()).fetch(false))
        );
```

in Kotlin

```kotlin
joiner.findOne(group innerJoin user on { it.name.isNotNull() })
```

## Nested joins

**Remark**: Kotlin API greatly improves read-ability of nested joins, see details below

Nested joins look following:

```java
joiner.findOne(Q.from(QGroup.group)
                 .joins(J.inner(QUser.user1).nested(QPhone.phone)));
```

Or even deeper:

```java
joiner.findOne(Q.from(QGroup.group)
                .joins(
                        J.inner(QUser.user1).nested(
                                J.left(QPhone.phone).nested(QStatus.status)
                        ),
                        
                        J.left(QStatus.status)
                ));
```

Joiner represents query joins as a graph, which allows to automatically resolve unique aliases for nested joins (even when there are name collisions in different tree branches).

Aliases of ambiguous aliases for joins are determined at runtime. `J.path(...)` allows to get alias of ambiguous join.

So from previous example, the phone can be referenced directly, by the phone statuses only using `J.path(...)`:       

```java
joiner.findOne(Q.from(QGroup.group)
                    .joins(
                            J.inner(QUser.user1).nested(
                                    J.left(QPhone.phone)
                                            .nested(QStatus.status)
                            ),
                            
                            J.left(QStatus.status)
                    )
                    .where(QPhone.phone.type.eq("mobile")
                                            .and(J.path(QUser.user1, QPhone.phone, QStatus.status).active.isTrue())));
```

Or, you may use a unique name:

```java
joiner.findOne(Q.from(QGroup.group)
                    .joins(
                            J.inner(QUser.user1).nested(
                                    J.left(QPhone.phone)
                                            .nested(new QStatus("contactStatus"))
                            ),
                            
                            J.left(QStatus.status)
                    )
                    .where(QPhone.phone.type.eq("mobile")
                                            .and(new QStatus("contactStatus").active.isTrue())));
```

If the target join is at second level, it may be referenced via parent like this:

```java
joiner.findOne(Q.from(QGroup.group)
        .joins(
            J.inner(QUser.user1).nested(J.left(QStatus.status)),
            J.left(QStatus.status)
        )
        .where(QPhone.phone.type.eq("mobile")
        .and(J.path(QUser.user1.statuses).active.isTrue())));
```

## Inheritance

Joining a subclass only (`SuperUser` extends `User`):

```java
joiner.findOne(Q.from(QGroup.group)
                  .joins(QSuperUser.superUser)
                  .where(QGroup.group.id.eq(1L)));
```

Joining an attribute, which is present on a subclass only (`Key` is present on `SuperUser` only)

```java
joiner.findOne(Q.from(QGroup.group)
                  .joins(J.left(QSuperUser.superUser)
                  .nested(QKey.key))
                  .where(QGroup.group.id.eq(1L)));
```

## Result projection

By default, `find` and `findOne` return an object(s) of type passed to `from` method. Customizing of result projection
is possible using `Q.select` method. Lets find the active phone number of John:

```java
String number = joiner.findOne(Q.select(phone.number)
                                  .from(user)
                                  .joins(J.inner(phone).nested(status))
                                  .where(user.name.eq("John").and(status.active.isTrue()))
        );
```

Or tuple:

```java
List<Tuple> tuple = joiner.findOne(user.surname, Q.select(phone.number)
                                      .from(user)
                                      .joins(J.inner(phone).nested(status))
                                      .where(user.name.eq("John").and(status.active.isTrue()))
        );
```

in Kotlin:

```kotlin
val number = joiner.findOne(phone.number from user
        innerJoin (phone leftJoin status)
        where { user.name eq "John" and status.active eq true }
)
```

## Sorting

```java
joiner.findOne(Q.from(QGroup.group)
                  .asc(QGroup.group.name));
```

```java
joiner.findOne(Q.from(QGroup.group)
                 .desc(QGroup.group.name,QGroup.group.id));
```

in Kotlin

```kotlin
joiner.findOne(group.all()
                asc group.name
)
```

## Query features

Query features allow to modify the request/query before executing in declarative way.   
For example, joiner offers a build-it query feature for spring-based pagination - PageableFeature.  
Usage of the features is following:

```java
joiner.findOne(Q.from(QGroup.group)
        .addFeatures(new PageableFeature(PageRequest.of(0,20))));
```

You can implement your own features, for example a feature which adds active status predicate to all present joins:
```java
public class ActiveStatusFeature implements QueryFeature {

    @Override
    public <T, R> JoinerQuery<T, R> before(JoinerQuery<T, R> request) {
        J.unrollChildrenJoins(request.getJoins()).forEach(j -> {
            // Find status field
            BooleanPath active = ReflectionUtils.getField(j.getAlias(), "active", BooleanPath.class);

            // Add predicate to "on" clause
            j.on(active.isTrue().and(j.getOn()));
        });

        return request;
    }

}
```

## Kotlin API showcase

With Kotlin, it is possible to introduce even more fluent API. It supports the same set of features and brings better
core read-ability. Kotlin query builder is 100% compatible with existing java `Joiner` class and spring data
repositories.

This example demonstrates different ways of making a join:

```kotlin
import some.model.QUser.user

val userNames = joiner.findOne(user.name from user
        leftJoin user.addresses
        innerJoin QPhone.phone
        leftJoin (QGroup.group innerJoin QStatus.status)

        where { it.name eq "user1" and it.id notIn listOf(1, 2) }
        limit 5

        asc user.id
)
```

where

- `QUser.user1.name from QUser.user1` specifies the result projection (names of users) and target entity (user)
- `leftJoin QUser.user1.addresses` and `innerJoin QPhone.phone` join can be set as a path via parent (like joining user
  addresses via `QUser.user1.addresses`) or via entity alias (`QPhone.phone`)
- `leftJoin (QGroup.group innerJoin QStatus.status)` nested joins are much easier to read&write now, those are just
  marked by brackets
- `where { it.name eq "user1" and it.id notIn listOf(1, 2) }` root entity is passed as a param, so it can be accessed
  directly (`it.name` instead of `QUser.user.name`), all operators are supported as infix functions

### Select all and count queries

Result projection can be omitted by using `QUser.user.all() where { ... }`. Count query is created
via `QUser.user.countOf() where { ... }`.

### Importing Kotlin API

As of now, Intellij IDEA may struggle with finding correct imports for Joiner infix & extension functions, so it may be
needed to add those manually:

```kotlin
import cz.encircled.joiner.kotlin.JoinerKtOps.innerJoin
import cz.encircled.joiner.kotlin.JoinerKtOps.leftJoin
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
```

In some cases, it might be more convenient to avoid direct imports, especially due to autocompletion in IDEA. For
instance, when a class has a lot of queries. It can be done by implementing
interface `cz.encircled.joiner.kotlin.JoinOps` like `class YourRepository : JoinOps { ... }`

## Reactive API

### Project Reactor

Joiner provides reactive API (currently Project Reactor) by using Hibernate Reactive under the hood.

Reactive API is available via `cz.encircled.joiner.reactive.ReactorJoiner` class, providing flux/mono functions for
insert and search operations. See full demo app in the `example` folder

Sample queries, executed in a single DB transaction:

```kotlin
/**
 * Create super users for applicable users
 */
fun createSuperUsersIsApplicable(ids : List<Long>): Flux<SuperUser> {
    return reactorJoiner.transaction { 
        find(user.name from user where { it.id isIn ids })
            .filter { name -> ... }
            .map { name -> SuperUser(name) }
            .persistMultiple { it }
    }
}

```

# Example setup

As per [QueryDSL documentation](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration),
`apt-maven-plugin` must be used to generate a metamodel of entities (so called Q-classes).

Then all you need is an instance of JPA entity manager (via `Hibernate` or `Eclipselink`), setup of Joiner is as simple as:

```java
Joiner joiner = new Joiner(getEntityManager());

joiner.find(Q.from(QUser.user)
        .where(QUser.user.name.isNotNull()));
```

or in Kotlin

```kotlin
val joiner: Joiner = Joiner(getEntityManager())

joiner.find(QUser.user.all()
        where { it.name eq "John" })
```

## Reactive setup

Reactive API supports Hibernate only, its initialization is very similar and requires `javax.persistence.EntityManagerFactory`:
```java
ReactorJoiner joiner = new ReactorJoiner(getEntityManagerFactory())
...
```

Also, to set up Reactive Joiner, you must have following dependencies on the classpath:

Eclipse vertx driver for target database, for instance for mysql:
```xml
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-mysql-client</artifactId>
    <version>${vertx.version}</version>
</dependency>
```

In case of Project Reactor Joiner, you must have it on the classpath as well:
```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
    <version>${reactor.version}</version>
</dependency>
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
</dependency>
```

### Kotlin module

```xml

<dependency>
  <groupId>cz.encircled</groupId>
  <artifactId>joiner-kotlin</artifactId>
  <version>${joiner.version}</version>
</dependency>
```

### Project Reactor module

```xml

<dependency>
  <groupId>cz.encircled</groupId>
  <artifactId>joiner-reactive</artifactId>
  <version>${joiner.version}</version>
</dependency>
```

### Kotlin coroutines module

```xml

<dependency>
  <groupId>cz.encircled</groupId>
  <artifactId>joiner-kotlin-reactive</artifactId>
  <version>${joiner.version}</version>
</dependency>
```
