The main goal of **Joiner** is to make [QueryDSL](http://www.querydsl.com/) even more useful. 
**Joiner** is beneficial for applications with complex domain model, it provides:
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