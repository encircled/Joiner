# Joiner

Joiner is an addition for [QueryDSL](http://www.querydsl.com/) JPA framework. QueryDSL itself enables the construction of type-safe SQL-like queries in Java. Joiner has similar API for query construction, but unlike QueryDSL it is focused on applications with complex domain model, which require a lot of work with query joins.   

Joiner can be used instead of or together with QueryDSL. Joiner uses QueryDSL internally and expects that QueryDSL maven APT plugin is used for entities metamodel. See more about QueryDSL installation at [QueryDSL](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration).

Joiner offers following extra features:
* a simple way of adding complex joins to the jpa queries
* helps to resolve alias uniqueness in the queries
* correct join fetching in Hibernate and Eclipselink as well
* separating QueryDSL `JPAQuery` and query parameters


## Examples

Joiner allows specifying nested joins and resolves alias uniqueness for you

```
List<Group> groups = joiner.find(Q.from(QGroup.group)
                                            .joins(J.inner(QSuperUser.superUser)
                                                .nested(J.left(QUserKey.userKey), J.left(QStatus.status)))
                                            .joins(J.left(QStatus.status))
                                    );
```

As you can see, `statuses` are present on user and on group entities. Aliases are resolved automatically and can be referenced in query predicates, for example in `where`:

```
List<Group> groups = joiner.find(Q.from(QGroup.group)
                                            .joins(J.inner(QSuperUser.superUser)
                                                    .nested(J.left(QUserKey.userKey), J.left(QStatus.status)))
                                            .joins(J.left(QStatus.status))
                                            .where(QStatus.status.name.eq("GroupStatus")
                                                    .or(J.path(QSuperUser.superUser, QStatus.status).name.eq("UserStatus")))
                                    );
```

## Maven dependencies  

### Core module
```
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-core</artifactId>
    <version>0.2</version>
</dependency>
```

### Spring integration support module
```
<dependency>
    <groupId>cz.encircled</groupId>
    <artifactId>joiner-spring</artifactId>
    <version>0.2</version>
</dependency>
```
