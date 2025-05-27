package cz.encircled.joiner.core;

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

import static cz.encircled.joiner.model.QUser.user1;
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
        String jpql = serializer.serialize(query);
        assertEquals("select distinct user1 from User user1", jpql);
        assertConstants(serializer);
    }

    @Test
    public void testDtoProjection() {
        JoinerQuery<?, ?> query = Q.select(ProjectionTest.class, user.name, user.id).from(user);
        String jpql = serializer.serialize(query);
        assertEquals("select distinct user1.name, user1.id from User user1", jpql);
        assertConstants(serializer);
    }

    @Test
    public void testBasicQueryWithCustomAlias() {
        QUser myUser = new QUser("my_user");
        JoinerQuery<?, ?> query = Q.select(myUser.name).from(myUser);
        String jpql = serializer.serialize(query);
        assertEquals("select distinct my_user.name from User my_user", jpql);
        assertConstants(serializer);
    }

    @Test
    public void testCountQuery() {
        JoinerQuery<?, ?> query = Q.count(user);
        String jpql = serializer.serialize(query);
        assertEquals("select count(user1) from User user1", jpql);
        assertConstants(serializer);
    }

    @Test
    public void testWhereClause() {
        JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John"));
        String jpql = serializer.serialize(query);
        assertEquals("select distinct user1 from User user1 where user1.name = ?1", jpql);
        assertConstants(serializer, "John");
    }

    @Nested
    class ExpressionOperators {

        @Test
        public void inWithSingleParam() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.in(0L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id = ?1", jpql);
            assertConstants(serializer, 0L);
        }

        @Test
        public void inWithMultipleParam() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.in(0L, 1L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id in ?1", jpql);
            assertConstants(serializer, List.of(0L, 1L));
        }

        @Test
        public void notInWithSingleParam() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.notIn(0L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id <> ?1", jpql);
            assertConstants(serializer, 0L);
        }

        @Test
        public void notInWithMultipleParam() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.notIn(0L, 1L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id not in ?1", jpql);
            assertConstants(serializer, List.of(0L, 1L));
        }

        @Test
        public void isNull() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.isNull());
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id is null", jpql);
            assertConstants(serializer);
        }

        @Test
        public void startsWith() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.startsWith("J"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name like ?1", jpql);
            assertConstants(serializer, "J%");
        }

        @Test
        public void startsWithIgnoreCase() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.startsWithIgnoreCase("J"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name like ?1", jpql);
            assertConstants(serializer, "J%");
        }

        @Test
        public void endsWith() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.endsWith("hn"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name like ?1", jpql);
            assertConstants(serializer, "%hn");
        }

        @Test
        public void endsWithIgnoreCase() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.endsWithIgnoreCase("hn"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name like ?1", jpql);
            assertConstants(serializer, "%hn");
        }

        @Test
        public void contains() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.contains("oh"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name like ?1", jpql);
            assertConstants(serializer, "%oh%");
        }

        @Test
        public void containsIgnoreCase() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.containsIgnoreCase("oh"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name like ?1", jpql);
            assertConstants(serializer, "%oh%");
        }

        @Test
        public void like() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.like("J%n"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name like ?1", jpql);
            assertConstants(serializer, "J%n");
        }

        @Test
        public void likeIgnoreCase() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.likeIgnoreCase("J%n"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name like ?1", jpql);
            assertConstants(serializer, "J%n");
        }

    }

    @Nested
    class Joins {

        @Test
        public void leftJoins() {
            JoinerQuery<?, ?> query = Q.from(user).joins(J.left(user.groups).collectionPath(user.groups));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 left join fetch user1.groups group1", jpql);
        }

        @Test
        public void innerJoins() {
            JoinerQuery<?, ?> query = Q.from(user).joins(J.inner(user.groups).collectionPath(user.groups));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 inner join fetch user1.groups group1", jpql);
        }

        @Test
        public void testNestedJoins() {
            JoinerQuery<?, ?> query = Q.from(user).joins(J.left(user.groups).collectionPath(user.groups).nested(J.left(group.users).collectionPath(group.users)));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 left join fetch user1.groups group1 left join fetch group1.users user1_on_group1", jpql);
        }

        @Test
        public void testJoinsInPredicate() {
            JoinerQuery<?, ?> query = Q.from(QAddress.address)
                    .where(QAddress.address.user.id.ne(Q.select(user1.id.max()).from(QGroup.group).joins(J.inner(QGroup.group.users))));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct address from Address address where address.user.id <> (select distinct max(user1.id) from Group group1 inner join group1.users user1)", jpql);
        }

    }

    @Test
    public void testOrderBy() {
        JoinerQuery<?, ?> query = Q.from(user).asc(user.name);
        String jpql = serializer.serialize(query);
        assertEquals("select distinct user1 from User user1 order by user1.name asc", jpql);
        assertConstants(serializer);
    }

    @Test
    public void testGroupBy() {
        JoinerQuery<?, ?> query = Q.from(user).groupBy(user.name, user.id);
        String jpql = serializer.serialize(query);
        System.out.println(jpql);
        assertEquals("select distinct user1 from User user1 group by user1.name, user1.id", jpql);
    }

    @Test
    public void testHaving() {
        JoinerQuery<?, ?> query = Q.from(user).groupBy(user.name).having(user.id.gt(0L));
        String jpql = serializer.serialize(query);
        assertEquals("select distinct user1 from User user1 group by user1.name having user1.id > ?1", jpql);
        assertConstants(serializer, 0L);
    }

    @Test
    public void testPredicateWithConstants() {
        JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John").and(user.id.gt(10L)));

        String jpql = serializer.serialize(query);

        assertTrue(jpql.contains("user1.name = ?"));
        assertTrue(jpql.contains("user1.id > ?"));

        assertConstants(serializer, "John", 10L);
    }

    @Nested
    class FunctionProjection {

        @Test
        public void testAvgFunction() {
            // Create a query with avg function in the projection
            JoinerQuery<?, ?> query = Q.select(address.id.avg()).from(address).groupBy(address.user);

            // Serialize the query
            String jpql = serializer.serialize(query);
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
            String jpql = serializer.serialize(query);

            // Verify that the count function is correctly serialized
            assertTrue(jpql.contains("select distinct count(address.id)"));
            assertTrue(jpql.contains("group by address.user"));
        }

        @Test
        public void testMaxFunction() {
            // Create a query with max function in a projection
            JoinerQuery<?, ?> query = Q.select(address.id.max()).from(address).groupBy(address.user);

            // Serialize the query
            String jpql = serializer.serialize(query);
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
            String jpql = serializer.serialize(query);
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
            String jpql = serializer.serialize(query);
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

            String jpql = serializer.serialize(query);

            assertEquals("select distinct avg(address.id) from Address address group by address.user having count(address.id) > ?1", jpql);
            assertConstants(serializer, 2L);
        }
    }

    @Nested
    class SqlFunctions {

        @Test
        public void testUpperFunction() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.upper().eq("JOHN"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where upper(user1.name) = ?1", jpql);
            assertConstants(serializer, "JOHN");
        }

        @Test
        public void testLowerFunction() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.lower().eq("john"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where lower(user1.name) = ?1", jpql);
            assertConstants(serializer, "john");
        }

        @Test
        public void testLengthFunction() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.length().gt(5));
            String jpql = serializer.serialize(query);
            assertTrue(jpql.contains("where string_length(user1.name) > ?1") || 
                       jpql.contains("where length(user1.name) > ?1"));
            assertConstants(serializer, 5);
        }

        @Test
        public void testConcatFunction() {
            // Using StringExpression.concat
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.concat(" Smith").eq("John Smith"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where concat(user1.name, ?1) = ?2", jpql);
            assertConstants(serializer, " Smith", "John Smith");
        }

        @Test
        public void testSubstringFunction() {
            // Using StringExpression.substring
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.substring(1, 3).eq("oh"));
            String jpql = serializer.serialize(query);
            System.out.println("testSubstringFunction JPQL: " + jpql);
            assertTrue(jpql.contains("where") && 
                       ((jpql.contains("substring(user1.name") || jpql.contains("substr_2args(user1.name"))) && 
                       jpql.contains("= ?3"));
            assertConstants(serializer, 1, 3, "oh");
        }

        @Test
        public void testTrimFunction() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.trim().eq("John"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where trim(user1.name) = ?1", jpql);
            assertConstants(serializer, "John");
        }

        @Test
        public void testAbsFunction() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.abs().eq(10L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where abs(user1.id) = ?1", jpql);
            assertConstants(serializer, 10L);
        }

        @Test
        public void testSqrtFunction() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.sqrt().gt(3.0));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where sqrt(user1.id) > ?1", jpql);
            assertConstants(serializer, 3.0);
        }

        @Test
        public void testModFunction() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.mod(2L).eq(0L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where mod(user1.id, ?1) = ?2", jpql);
            assertConstants(serializer, 2L, 0L);
        }
    }

    @Nested
    class JpaOperators {

        @Test
        public void testBetween() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.between(1L, 10L));
            String jpql = serializer.serialize(query);
            assertTrue(jpql.contains("where between(user1.id, ?1, ?2)") || 
                       jpql.contains("where user1.id between ?1 and ?2"));
            assertConstants(serializer, 1L, 10L);
        }

        @Test
        public void testIsEmpty() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.groups.isEmpty());
            String jpql = serializer.serialize(query);
            assertTrue(jpql.contains("where col_is_empty(user1.groups)") || 
                       jpql.contains("where user1.groups is empty"));
            assertConstants(serializer);
        }

        @Test
        public void testIsNotEmpty() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.groups.isNotEmpty());
            String jpql = serializer.serialize(query);
            assertTrue(jpql.contains("where not col_is_empty(user1.groups)") || 
                       jpql.contains("where user1.groups is not empty"));
            assertConstants(serializer);
        }

        @Test
        public void testSize() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.groups.size().eq(2));
            String jpql = serializer.serialize(query);
            System.out.println("testSize JPQL: " + jpql);
            assertTrue(jpql.contains("where") && jpql.contains("user1.groups") && jpql.contains("= ?1"));
            assertConstants(serializer, 2);
        }

        @Test
        public void testCoalesce() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.coalesce("Unknown").eq("John"));
            String jpql = serializer.serialize(query);
            assertTrue(jpql.contains("where coalesce(user1.name") && 
                       jpql.contains("= ?2"));
            assertConstants(serializer, "Unknown", "John");
        }
    }

    @Nested
    class LogicalOperators {

        @Test
        public void testAnd() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John").and(user.id.gt(10L)));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name = ?1 and user1.id > ?2", jpql);
            assertConstants(serializer, "John", 10L);
        }

        @Test
        public void testOr() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John").or(user.id.gt(10L)));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name = ?1 or user1.id > ?2", jpql);
            assertConstants(serializer, "John", 10L);
        }

        @Test
        public void testNot() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John").not());
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where not user1.name = ?1", jpql);
            assertConstants(serializer, "John");
        }
    }

    @Nested
    class Subquery {
        @Test
        public void testSubQueryInPredicate() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .where(user.id.eq(2L).or(user.id.in(Q.select(address.user.id).from(address).where(address.id.eq(1L)))));

            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id = ?1 or user1.id in (select distinct address.user.id from Address address where address.id = ?2)", jpql);
            assertConstants(serializer, 2L, 1L);
        }

        @Test
        public void testNestedSubQueryInPredicate() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .where(user.id.ne(Q.select(user.id.max()).from(user)));

            String jpql = serializer.serialize(query);
            System.out.println("testNestedSubQueryInPredicate JPQL: " + jpql);

            assertEquals("select distinct user1 from User user1 where user1.id <> (select distinct max(user1.id) from User user1)", jpql);
            assertConstants(serializer);
        }
    }

    void assertConstants(JoinerJPQLSerializer serializer, Object... expected) {
        assertEquals(expected.length, serializer.getConstants().size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], serializer.getConstants().get(i));
        }
    }

    public static class ProjectionTest {
        public ProjectionTest(String name, Long id) {

        }
    }

}
