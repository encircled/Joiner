Joiner is an addition for [QueryDSL](http://www.querydsl.com/) JPA framework. QueryDSL itself enables the construction of type-safe SQL-like queries in Java. Joiner has similiar API for query construction, but unlike QueryDSL it is focused on applications with complex domain model, which require a lot of work with query joins.   

Joiner can be used instead of or together with QueryDSL. Joiner uses QueryDSL internally and expects that QueryDSL maven APT plugin is used for entities metamodel. See more about QueryDSL installation at [QueryDSL](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration).

Joiner offers following extra features:
* a simple way to add joins to the QueryDSL queries at build or runtime
* helps to resolve alias uniqueness in the queries
* simple fetching of attributes, which are present on child entities only (for inheritance)
* separating QueryDSL `JPAQuery` and query parameters


***


For example:   
`List<Group> groups = groupRepository.find(new Q<Group>()`     
                `.joins(QGroup.group.users, QSuperUser.superUser.key, QNormalUser.normalUser.passwords)`     
                `.where(QKey.key.name.ne("bad_key")));`
       

`Group` has collection of `Users`, there are multiple children of class `User`. This code will add joins for users and nested attributes on users. 
