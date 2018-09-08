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

## Result projection

By default, `find` and `findOne` return an object(s) of type passed to `from` method. 
Customizing of result projection is possible using `Q.select` method. 
Lets find the active phone number of John:    
   
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
