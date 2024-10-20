# Spring Boot + Joiner + GraphQL

Using join graphs allows dynamically adding only the required joins to the query

See `JoinerConfig` and `AuthorGraphql` for configuration and usage examples.

### Author without associations

```graphql
query ScratchQuery {
    getAuthor(id: 1) {
        id,
        name,
    }
}
```

Generated JPA query:

```sql
select
    distinct a1_0.id,
    a1_0.name,
    a1_0.status_id 
from
    author a1_0 
where
    a1_0.id=?
```

### Author with status


```graphql
query ScratchQuery {
    getAuthor(id: 1) {
        name,
        status {
            name
        }
    }
}
```

Generated JPA query:

```sql
select
        distinct a1_0.id,
        a1_0.name,
        s1_0.id,
        s1_0.name 
    from
        author a1_0 
    join
        status s1_0 
            on s1_0.id=a1_0.status_id 
    where
        a1_0.id=?
```

### Query all nested elements

```graphql
query ScratchQuery {
    getAuthor(id: 1) {
        name,
        status {
            name
        }
        posts {
            name
            status {
                name
            }
        }
    }
}
```

Generated JPA query: 

```sql
select
        distinct a1_0.id,
        a1_0.name,
        p1_0.author_id,
        p1_0.id,
        p1_0.name,
        s1_0.id,
        s1_0.name,
        s2_0.id,
        s2_0.name 
    from
        author a1_0 
    left join
        post p1_0 
            on a1_0.id=p1_0.author_id 
    left join
        status s1_0 
            on s1_0.id=p1_0.status_id 
    join
        status s2_0 
            on s2_0.id=a1_0.status_id 
    where
        a1_0.id=?
```