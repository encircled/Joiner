Joiner is an addition for [QueryDSL](http://www.querydsl.com/) JPA framework. QueryDSL itself enables the construction of type-safe SQL-like queries in Java. Joiner has similiar API for query construction, but unlike QueryDSL it is focused on applications with complex domain model, which require a lot of work with query joins.   

Joiner can be used instead of or together with QueryDSL. Joiner uses QueryDSL internally and expects that QueryDSL maven APT plugin is used for entities metamodel. See more about QueryDSL installation at [QueryDSL](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration).

Joiner offers following extra features:
* a simple way of adding complex joins to the jpa queries
* helps to resolve alias uniqueness in the queries
* correct join fetching in Hibernate and Eclipselink as well
* separating QueryDSL `JPAQuery` and query parameters

