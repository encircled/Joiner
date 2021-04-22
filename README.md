[![Build Status](https://travis-ci.org/encircled/Joiner.svg?branch=master)](https://travis-ci.org/encircled/Joiner)
[![codecov](https://codecov.io/gh/encircled/Joiner/branch/master/graph/badge.svg)](https://codecov.io/gh/encircled/Joiner)

# Overview

Joiner is a Java library which allows to create type-safe JPA queries. It is focused on applications with complex domain model, which require a lot of work with query joins.   

Joiner can be used instead of or together with QueryDSL. Joiner uses QueryDSL APT maven plugin for entity metamodel generation. See more about QueryDSL installation at [QueryDSL](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration).

Joiner offers following extra features:
* simple way of adding complex joins to the queries
* automatic resolving of alias uniqueness in queries
* fixed join fetching in Eclipselink (when using inheritance)

# Example setup

All you need is an instance of entity manager, setup of Joiner is as simple as:

```java
Joiner joiner = new Joiner(getEntityManager());
joiner.find(Q.from(QUser.user)
                .where(QUser.user.isNotNull()));
```

# Features

## Basic query

```java
joiner.find(Q.from(QGroup.group)
                .where(QGroup.group.id.eq(1L))
                .groupBy(QGroup.group.type)
                .limit(10)
                .offset(2)     
                .distinct());
```

## Basic join

Example below shows how to join users of group. Target attribute is looked up by type and field name, so it does not matter which relationship it is:

```java
joiner.findOne(Q.from(QGroup.group)
                .joins(QUser.user);
```

By default, all joins are left fetch joins. If there are multiple field with the same type, a name should be specified explicitly:

```java
joiner.findOne(Q.from(QGroup.group)
                .joins(J.inner(new QUser("userAttrName"))
                                    .on(new QUser("userAttrName").name.isNotNull())
                                    .fetch(false)));
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
                                J.left(QPhone.phone)
                                        .nested(QStatus.status)
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

## Result projection

By default, `find` and `findOne` return an object(s) of type passed to `from` method. Customizing of result projection
is possible using `Q.select` method. Lets find the active phone number of John:

```java
String number = joiner.findOne(Q.select(QPhone.phone.number)
                    .from(QUser.user)
                    .joins(J.inner(QPhone.phone).nested(QStatus.status))
                    .where(QUser.user.name.eq("John").and(QStatus.status.active.isTrue()))
                    );
```

Or tuple:

```java
List<Tuple> tuple = joiner.findOne(QUser.user.surname, Q.select(QPhone.phone.number)
                            .from(QUser.user)
                            .joins(J.inner(QPhone.phone).nested(QStatus.status))
                            .where(QUser.user.name.eq("John").and(QStatus.status.active.isTrue()))
                            );
```

## Sorting

```java
joiner.findOne(Q.from(QGroup.group)
                .asc(QGroup.group.name));
```

```java
joiner.findOne(Q.from(QGroup.group)
                .desc(QGroup.group.name, QGroup.group.id));
```

## Query features

Query features allow to modify the request/query before executing in declarative way.   
For example, joiner offers a build-it query feature for spring-based pagination - PageableFeature.  
Usage of the features is following:

```java
joiner.findOne(Q.from(QGroup.group)
                .addFeatures(new PageableFeature(PageRequest.of(0, 20))));
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

## Kotlin API

With Kotlin, it is possible to introduce even more fluent API. It supports the same set of features and brings better
core read-ability. Kotlin query builder is 100% compatible with existing java `Joiner` class and spring data
repositories.

Kotlin query showcase:

```kotlin
        val userNames = joiner.findOne(
                QUser.user1.name from QUser.user1
                        leftJoin QUser.user1.addresses
                        innerJoin QPhone.phone
                        leftJoin (QGroup.group innerJoin QStatus.status)
            
                        where { it.name eq "user1" and it.id notIn listOf(1, 2) }
                        limit 5
            
                        asc QUser.user1.id
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
import cz.encircled.joiner.kotlin.QueryBuilder.all
import cz.encircled.joiner.kotlin.QueryBuilder.countOf
import cz.encircled.joiner.kotlin.QueryBuilder.from
```

In some cases, it might be more convenient to avoid direct imports. For instance, when a class has a lot of queries. As
of now, it will improve autocompletion in IDEA. It can be done by implementing
interface `cz.encircled.joiner.kotlin.JoinOps`

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

### Kotlin module
```xml
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-kotlin</artifactId>
    <version>${joiner.version}</version>
</depend
