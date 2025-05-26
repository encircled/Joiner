package cz.encircled.joiner.core;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    public void testDtoProjection() {
        JoinerQuery<?, ?> query = Q.select(ProjectionTest.class, user.name, user.id).from(user);
        String jpql = serializer.serialize(query, false);
        assertEquals("select distinct user1.name, user1.id from User user1 ", jpql);
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
        assertEquals("select distinct user1 from User user1  where user1.name = ?1", jpql);
        assertEquals(1, serializer.getConstants().size());
        assertEquals("John", serializer.getConstants().get(0));
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
        assertEquals("select distinct user1 from User user1  left join fetch user1.groups as group1 left join fetch group1.users as user1_on_group1", jpql);
        assertTrue(jpql.contains("left join fetch user1.groups"));
        assertTrue(jpql.contains("left join fetch group1.users"));
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
        assertEquals("select distinct user1 from User user1  group by user1.name having user1.id > ?1", jpql);
        assertEquals(0L, serializer.getConstants().get(0));
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

    @Test
    public void testPredicateWithConstants() {
        // Create a query with a predicate that contains constants
        JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John").and(user.id.gt(10L)));

        // Serialize the query
        String jpql = serializer.serialize(query, false);
        System.out.println("testPredicateWithConstants JPQL: " + jpql);

        // Verify that the constants are replaced with parameter placeholders
        assertTrue(jpql.contains("user1.name = ?"));
        assertTrue(jpql.contains("user1.id > ?"));

        // Verify that the constants are added to the constants list
        List<Object> constants = serializer.getConstants();
        assertEquals(2, constants.size());
        assertEquals("John", constants.get(0));
        assertEquals(10L, constants.get(1));
    }

    @Nested
    class FunctionProjection {

        @Test
        public void testAvgFunction() {
            // Create a query with avg function in projection
            JoinerQuery<?, ?> query = Q.select(address.id.avg()).from(address).groupBy(address.user);

            // Serialize the query
            String jpql = serializer.serialize(query, false);
            System.out.println("testAvgFunction JPQL: " + jpql);

            // Verify that the avg function is correctly serialized
            assertTrue(jpql.contains("select distinct avg(address.id)"));
            assertTrue(jpql.contains("group by address.user"));
        }

        @Test
        public void testCountFunction() {
            // Create a query with count function in projection
            JoinerQuery<?, ?> query = Q.select(address.id.count()).from(address).groupBy(address.user);

            // Serialize the query
            String jpql = serializer.serialize(query, false);
            System.out.println("testCountFunction JPQL: " + jpql);

            // Verify that the count function is correctly serialized
            assertTrue(jpql.contains("select distinct count(address.id)"));
            assertTrue(jpql.contains("group by address.user"));
        }

        @Test
        public void testMaxFunction() {
            // Create a query with max function in a projection
            JoinerQuery<?, ?> query = Q.select(address.id.max()).from(address).groupBy(address.user);

            // Serialize the query
            String jpql = serializer.serialize(query, false);
            System.out.println("testMaxFunction JPQL: " + jpql);

            // Verify that the max function is correctly serialized
            assertTrue(jpql.contains("select distinct max(address.id)"));
            assertTrue(jpql.contains("group by address.user"));
        }

        @Test
        public void testMinFunction() {
            // Create a query with min function in a projection
            JoinerQuery<?, ?> query = Q.select(address.id.min()).from(address).groupBy(address.user);

            // Serialize the query
            String jpql = serializer.serialize(query, false);
            System.out.println("testMinFunction JPQL: " + jpql);

            // Verify that the min function is correctly serialized
            assertTrue(jpql.contains("select distinct min(address.id)"));
            assertTrue(jpql.contains("group by address.user"));
        }

        @Test
        public void testSumFunction() {
            // Create a query with sum function in a projection
            JoinerQuery<?, ?> query = Q.select(address.id.sum()).from(address).groupBy(address.user);

            // Serialize the query
            String jpql = serializer.serialize(query, false);
            System.out.println("testSumFunction JPQL: " + jpql);

            // Verify that the sum function is correctly serialized
            assertTrue(jpql.contains("select distinct sum(address.id)"));
            assertTrue(jpql.contains("group by address.user"));
        }

        @Test
        public void testFunctionInHaving() {
            // Create a query with a function in having clause
            JoinerQuery<?, ?> query = Q.select(address.id.avg())
                    .from(address)
                    .groupBy(address.user)
                    .having(address.id.count().gt(2));

            // Serialize the query
            String jpql = serializer.serialize(query, false);
            System.out.println("testFunctionInHaving JPQL: " + jpql);

            // Verify that the function in having clause is correctly serialized
            assertTrue(jpql.contains("select distinct avg(address.id)"));
            assertTrue(jpql.contains("group by address.user"));
            assertTrue(jpql.contains("having count(address.id) > ?"));
        }
    }

    @Nested
    class Subquery {
        @Test
        public void testSubQueryInPredicate() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .where(user.id.eq(2L).or(user.id.in(Q.select(address.user.id).from(address).where(address.id.eq(1L)))));

            String jpql = serializer.serialize(query, false);
            assertEquals("select distinct user1 from User user1  where user1.id = ?1 or user1.id in (select distinct address.user.id from Address address  where address.id = ?2)", jpql);
            assertEquals(2, serializer.getConstants().size());
        }

        @Test
        public void testNestedSubQueryInPredicate() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .where(user.id.ne(Q.select(user.id.max()).from(user)));

            String jpql = serializer.serialize(query, false);
            System.out.println("testNestedSubQueryInPredicate JPQL: " + jpql);

            assertTrue(jpql.contains("select distinct user1 from"));
            assertTrue(jpql.contains("where user1.id <> ("));
            assertTrue(jpql.contains("select distinct max(user1.id) from"));
        }
    }

    public static class ProjectionTest {
        public ProjectionTest(String name, Long id) {

        }
    }

}
