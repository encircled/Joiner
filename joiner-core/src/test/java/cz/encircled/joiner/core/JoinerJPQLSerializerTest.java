package cz.encircled.joiner.core;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the JoinerJPQLSerializer class.
 */
public class JoinerJPQLSerializerTest {

    private JoinerJPQLSerializer serializer;
    private QUser user;
    private QGroup group;
    private QAddress address;

    @BeforeEach
    public void setUp() {
        serializer = new JoinerJPQLSerializer();
        user = QUser.user1;
        group = QGroup.group;
        address = QAddress.address;
    }

    @Test
    public void testBasicQuery() {
        JoinerQuery<?, ?> query = Q.from(user);
        String jpql = serializer.serialize(query, false);
        assertEquals("select distinct user1 from User user1 ", jpql);
    }

    @Test
    public void testBasicQueryWithCustomAlias() {
        QUser myUser = new QUser("my_user");
        JoinerQuery<?, ?> query = Q.select(myUser.name).from(myUser);
        String jpql = serializer.serialize(query, false);
        assertEquals("select distinct my_user.name from User my_user ", jpql);
    }

    @Test
    public void testCountQuery() {
        JoinerQuery<?, ?> query = Q.from(user);
        String jpql = serializer.serialize(query, true);
        assertEquals("select count(user1) from User user1 ", jpql);
    }

    @Test
    public void testWhereClause() {
        JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John"));
        String jpql = serializer.serialize(query, false);
        assertEquals("select distinct user1 from User user1  where user1.name = John", jpql);
    }

    @Test
    public void testJoins() {
        JoinerQuery<?, ?> query = Q.from(user).joins(J.left(user.groups).collectionPath(user.groups));
        String jpql = serializer.serialize(query, false);
        System.out.println("testJoins JPQL: " + jpql);
        assertEquals("select distinct user1 from User user1  left join fetch user1.groups as group1", jpql);
    }

    @Test
    public void testNestedJoins() {
        JoinerQuery<?, ?> query = Q.from(user).joins(J.left(user.groups).collectionPath(user.groups).nested(J.left(group.users).collectionPath(group.users)));
        String jpql = serializer.serialize(query, false);
        System.out.println("testNestedJoins JPQL: " + jpql);
        assertEquals("select distinct user1 from User user1  left join fetch user1.groups as group1 left join fetch group1.users as users", jpql);
        assertTrue(jpql.contains("left join user1.groups"));
        assertTrue(jpql.contains("left join group.users"));
    }

    @Test
    public void testOrderBy() {
        JoinerQuery<?, ?> query = Q.from(user).asc(user.name);
        String jpql = serializer.serialize(query, false);
        System.out.println("testOrderBy JPQL: " + jpql);
        assertTrue(jpql.contains("order by"));
        assertTrue(jpql.contains("user1.name asc"));
    }

    @Test
    public void testGroupBy() {
        JoinerQuery<?, ?> query = Q.from(user).groupBy(user.name, user.id);
        String jpql = serializer.serialize(query, false);
        System.out.println(jpql);
        assertEquals("select distinct user1 from User user1  group by user1.name, user1.id", jpql);
    }

    @Test
    public void testHaving() {
        JoinerQuery<?, ?> query = Q.from(user).groupBy(user.name).having(user.id.gt(0L));
        String jpql = serializer.serialize(query, false);
        System.out.println(jpql);
        assertEquals("select distinct user1 from User user1  group by user1.name having user1.id > 0", jpql);
    }

    @Test
    public void testQueryMetadataSerialization() {
        QueryMetadata metadata = new DefaultQueryMetadata();
        metadata.addJoin(com.querydsl.core.JoinType.DEFAULT, user);
        metadata.addWhere(user.name.eq("John"));

        serializer.serialize(metadata, false, null);
        String jpql = serializer.toString();

        System.out.println(jpql);
        assertTrue(jpql.contains("select"));
        assertTrue(jpql.contains("from User"));
        assertTrue(jpql.contains("where"));
    }

    @Test
    public void testQueryMetadataWithProjection() {
        QueryMetadata metadata = new DefaultQueryMetadata();
        metadata.addJoin(com.querydsl.core.JoinType.DEFAULT, user);
        metadata.setProjection(user.name);

        serializer.serialize(metadata, false, null);
        String jpql = serializer.toString();
        assertEquals("select user1.name from User user1 ", jpql);
    }

    @Test
    public void testQueryMetadataCount() {
        QueryMetadata metadata = new DefaultQueryMetadata();
        metadata.addJoin(com.querydsl.core.JoinType.DEFAULT, user);

        serializer.serialize(metadata, true, null);
        String jpql = serializer.toString();

        assertTrue(jpql.contains("select count("));
        assertTrue(jpql.contains("from"));
    }
}
