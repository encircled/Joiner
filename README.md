[![CI build](https://github.com/encircled/Joiner/actions/workflows/run-tests-action.yml/badge.svg)](https://github.com/encircled/Joiner/actions/workflows/run-tests-action.yml)
[![codecov](https://codecov.io/gh/encircled/Joiner/branch/master/graph/badge.svg)](https://codecov.io/gh/encircled/Joiner)
[![Maven Central](https://img.shields.io/maven-central/v/cz.encircled/joiner-core.svg?label=Maven%20Central)](https://search.maven.org/artifact/cz.encircled/joiner-core/1.15/jar)

# Overview

Joiner is a Java library that enables the creation of type-safe JPA queries. It is designed for applications with complex domain models that require extensive use of query joins.

Joiner can be used either as a replacement for, or in conjunction with, QueryDSL. It leverages the QueryDSL for entity metamodel generation. See more about QueryDSL installation
at [QueryDSL](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration).

Joiner provides the following additional features:

* Type-safe database queries with autocompletion
* A simple way to add complex joins to queries
* Fluent Kotlin API
* Coroutines & Reactor using Hibernate Reactive
* Queries intercepting using QueryFeature API
* User's JoinGraphs for streamlining the addition of multiple joins to queries 
* Fixed compatibility issues when using QueryDSL 5 with Spring Boot 3 
* Fixed join fetching in EclipseLink (when using inheritance)
* Fixed high security vulnerability [CVE-2024-49203](https://github.com/advisories/GHSA-6q3q-6v5j-h6vg)

0.4.7 is the last Joiner version for javax API and Hibernate 5.

Joiner offers Java, Kotlin and reactive API, which are described below

### Readme

- [TL;DR](#tldr)
- [Basic query](#basic-query)
- [Basic joins](#basic-join)
- [Subquery](#subquery)
- [Customizing a join](#customizing-a-join)
- [Nested joins](#nested-joins)
- [Entity inheritance](#inheritance)
- [Result projection](#result-projection)
- [Sorting](#sorting)
- [Query features](#query-features)
- [Kotlin API showcase](#kotlin-api-showcase)
- [Reactive API](#reactive-api)
- [Example setup](#example-setup)
- [Example with GraphQL](#example-setup-graphql)
- [Maven dependencies](#maven-dependencies)

# TL;DR

Ultimately, all database queries are type-safe, support auto-completion, and look like this:

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

See example projects in https://github.com/encircled/Joiner/tree/master/example

# Features

## Basic query

```java
QGroup group = QGroup.group;

joiner.find(Q.select(group.type).from(group)
                .where(group.id.eq(1L))
                .groupBy(group.type)
                .limit(10)
                .offset(2));
```

or in Kotlin

```kotlin
joiner.find(group.type from group
                          where { it.id eq 1 }
                          groupBy { it.type }
                          limit 10
                          offset 2
)
```

## Subquery

Subqueries follow the same syntax as standard queries. For example:

```java
Q.select(address.city).from(address)
        .where(address.user.id.ne(Q.select(user.id.max()).from(user)))
```

or in Kotlin

```kotlin
address.city from address
        where { it.user.id ne (user.id.max() from user) }
```

## Basic join

The example below shows how to join users of a group. The target attribute is identified by type and field name, so the specific type of relationship does not matter:

```java
joiner.findOne(Q.from(QGroup.group)
                  .joins(QUser.user);
```

Aliases can be imported or extracted as variables to make the code cleaner and more readable:

```java
joiner.findOne(Q.from(group).joins(user));
```

By default, all joins are left fetch joins.

If there are multiple fields of the same type, the name must be specified explicitly. For instance, if a group has `user1` and `user2` fields, the correct approach would be:

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

To perform an inner join or create a non-fetch join (which will not be part of the result set):

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

**Remark**: The Kotlin API greatly improves the readability of nested joins. See details below:

Nested joins look as follows:

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

Joiner represents query joins as a graph, which allows automatic resolution of unique aliases for nested joins (even when name collisions occur in different branches of the tree).

Aliases for ambiguous joins are determined at runtime. `J.path(...)` allows you to retrieve the alias of such a join. However, it is often better to define and use a custom unique alias.

In the previous example, the phone can be referenced directly, but the phone statuses can only be accessed using `J.path(...)` or a custom unique alias:       

Unique name:
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

`J.path()`:
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


If the target join is at the second level, it can also be referenced through the parent:

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

The following query joins only the subclass (`SuperUser`, which extends `User`):

```java
joiner.findOne(Q.from(QGroup.group)
                  .joins(QSuperUser.superUser)
                  .where(QGroup.group.id.eq(1L)));
```

The following query joins a nested association that exists only on a subclass (`Key` is present only on `SuperUser`):

```java
joiner.findOne(Q.from(QGroup.group)
                  .joins(J.left(QSuperUser.superUser)
                        .nested(QKey.key))
                  .where(QGroup.group.id.eq(1L)));
```

## Result projection

By default, `find` and `findOne` return an object (or objects) of the type passed to the `from` method. Customizing the result projection 
is possible using the `Q.select` method. For example, to select a single object, such as the active phone number of John:

```java
String number = joiner.findOne(Q.select(phone.number)
                                  .from(user)
                                  .joins(J.inner(phone).nested(status))
                                  .where(user.name.eq("John").and(status.active.isTrue()))
        );
```

Or a tuple:

```java
List<Tuple> tuple = joiner.findOne(Q.select(user.firstName, user.lastName, phone.number)
                                      .from(user)
                                      .joins(J.inner(phone).nested(status))
                                      .where(user.name.eq("John").and(status.active.isTrue()))
        );
String number = tuple.get(0).get(phone.number);
```

A custom result projection can be mapped to a DTO object:

```java
List<TestDto> dto = joiner.find(Q.select(TestDto.class, user.id, user.name).from(user));

public static class TestDto {
  public Long id;
  public String name;

  public TestDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
```

in Kotlin:

```kotlin
val number = joiner.findOne(phone.number from user
        innerJoin (phone leftJoin status)
        where { user.name eq "John" and status.active eq true }
)
```

```kotlin
val dto = joinerKt.getOne(
            listOf(user.id, user.name)
                    mappingTo TestDto::class
                    from user
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

Query features allow you to modify the request/query in a declarative way before execution.

### Built-in features

Joiner offers a built-in query feature for Spring-based pagination: `PageableFeature`.
The usage is as follows:

```java
joiner.findOne(Q.from(QGroup.group)
        .addFeatures(new PageableFeature(PageRequest.of(0, 20))));
```

This will apply limiting and sorting parameters from the Spring page request. 


Another built-in feature is `PostQueryLazyFetchBlockerFeature`, use it to prevent uninitialized lazy attributes from being fetched when accessed.
```java
Group group = joiner.findOne(Q.from(QGroup.group)
        .addFeatures(new PostQueryLazyFetchBlockerFeature(entityManager)));

// This will not trigger lazy initialization of 'users'; instead, it will return an empty collection. 
// See the Javadoc for more details.
group.getUsers().size(); 
```

### Custom query features

You can implement your own features, such as a feature that adds an active status predicate to all existing joins:

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

With Kotlin, it’s possible to introduce an even more fluent API. It supports the same set of features while offering improved readability. 
The Kotlin query builder is fully compatible with the existing Java `Joiner` class and Spring Data repositories.

This example demonstrates various ways to perform a join:

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

- `QUser.user1.name from QUser.user1` specifies both the result projection (the names of users) and the target entity (user).
- `leftJoin QUser.user1.addresses` and `innerJoin QPhone.phone` can be set as a path through the parent (e.g., joining user addresses via `QUser.user1.addresses`) or through an entity alias (e.g. `QPhone.phone`)
- `leftJoin (QGroup.group innerJoin QStatus.status)` makes nested joins much easier to read and write, as they are simply marked by parentheses.
- `where { it.name eq "user1" and it.id notIn listOf(1, 2) }` the root entity is passed as a parameter, allowing direct access (`it.name` instead of `QUser.user.name`). All operators support infix function syntax.

### Select all and count queries

The result projection can be omitted by using `QUser.user.all() where { ... }`. A count query is created with `QUser.user.countOf() where { ... }`.

### Importing Kotlin API

Currently, IntelliJ IDEA may struggle to find the correct imports for Joiner infix and extension functions, so you may need to add them manually:

```kotlin
import cz.encircled.joiner.kotlin.JoinerKtOps.innerJoin
import cz.encircled.joiner.kotlin.JoinerKtOps.leftJoin
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
```

In some cases, it might be more convenient to avoid direct imports, especially due to IntelliJ IDEA's autocompletion, such as when a class contains many queries. 
This can be achieved by implementing the `cz.encircled.joiner.kotlin.JoinOps` interface, like so: `class YourRepository : JoinOps { ... }`

## Reactive API

### Project Reactor

Joiner provides a reactive API (currently based on Project Reactor) by utilizing Hibernate Reactive under the hood.

The reactive API is available through the `cz.encircled.joiner.reactive.ReactorJoiner` class, offering Flux/Mono functions for insert and search operations. 
A full demo app can be found in the `example` folder.

Sample queries, executed within a single database transaction:

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

See example projects in https://github.com/encircled/Joiner/tree/master/example

# Example setup GraphQL

Joiner can be used for dynamic adding required joins to the queries when using with GraphQL, see an example for more details https://github.com/encircled/Joiner/tree/master/example/spring-boot-graphql

### QueryDSL dependencies

Include `QueryDSL` dependencies:
```xml
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-jpa</artifactId>
    <classifier>jakarta</classifier>
    <version>${querydsl.version}</version>
</dependency>
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-apt</artifactId>
    <classifier>jakarta</classifier>
    <version>${querydsl.version}</version>
</dependency>
```

### Hibernate 5 additional setup

For `Hibernate 5` and below it is also required to add a `apt-maven-plugin` plugin for generation a metamodel (so called Q-classes):  

visit [QueryDSL documentation](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration) for detais.

### Joiner instantiation

Instantiate a JPA entity manager (via `Hibernate` or `EclipseLink`), and setting up Joiner is as simple as:

```java
Joiner joiner = new Joiner(getEntityManager());

joiner.find(Q.from(QUser.user)
        .where(QUser.user.name.isNotNull()));
```

or in Kotlin

```kotlin
val joiner: JoinerKt = JoinerKt(getEntityManager())

joiner.find(QUser.user.all()
        where { it.name eq "John" })
```

## Reactive setup

The reactive API supports Hibernate only, and its initialization is quite similar and requires `jakarta.persistence.EntityManagerFactory`:
```java
ReactorJoiner joiner = new ReactorJoiner(getEntityManagerFactory())
...
```

Additionally, to set up Reactive Joiner, you must include the following dependencies on the classpath:

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
