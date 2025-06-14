package cz.encircled.joiner.core;

import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cz.encircled.joiner.model.QAddress.address;
import static cz.encircled.joiner.model.QGroup.group;
import static cz.encircled.joiner.model.QUser.user1;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the JoinerJPQLSerializer class.
 */
public class JoinerJPQLSerializerTest {

    private JoinerJPQLSerializer serializer;
    private final QUser user = QUser.user1;

    @BeforeEach
    public void setUp() {
        serializer = new JoinerJPQLSerializer();
    }

    @Test
    public void testToString() {
        JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John"));
        assertEquals("select distinct user1 from User user1 where user1.name = ?1", query.toString());
    }

    @Nested
    class PredicateChains {

        @Test
        public void singleWhereClause() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name = ?1", jpql);
            assertConstants(serializer, "John");
        }

        @Test
        public void chainOfSameOperators() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John").and(user.active.isTrue()));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name = ?1 and user1.active = ?2", jpql);
            assertConstants(serializer, "John", true);
        }

        @Test
        public void chainOfSameOperatorsWithParentheses() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.eq("John"))
                    .andWhere(user.id.eq(1L).and(user.user.isNull()));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where (user1.id = ?1 and user1.user is null) and user1.name = ?2", jpql);
            assertConstants(serializer, 1L, "John");
        }

        @Test
        public void orThenAndChain() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .andWhere(user.name.eq("John").or(user.name.eq("Kate")))
                    .andWhere(user.id.gt(1));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id > ?1 and (user1.name = ?2 or user1.name = ?3)", jpql);
            assertConstants(serializer, 1L, "John", "Kate");
        }

        @Test
        public void orWithNestedAndClause() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .andWhere(
                            user.id.eq(1L)
                                    .or(user.name.eq("John").and(user.id.gt(10)))
                    );
            String jpql = serializer.serialize(query);
            assertEquals(
                    "select distinct user1 from User user1 where user1.id = ?1 or (user1.name = ?2 and user1.id > ?3)",
                    jpql
            );
            assertConstants(serializer, 1L, "John", 10L);
        }

        @Test
        public void multipleNestedClauses() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .andWhere(
                            user.name.eq("Alice")
                                    .or(
                                            user.name.eq("Bob")
                                                    .and(
                                                            user.id.eq(7L)
                                                                    .or(user.id.lt(5))
                                                    )
                                    )
                    );
            String jpql = serializer.serialize(query);
            assertEquals(
                    "select distinct user1 from User user1 where user1.name = ?1 or (user1.name = ?2 and (user1.id = ?3 or user1.id < ?4))",
                    jpql
            );
            assertConstants(serializer, "Alice", "Bob", 7L, 5L);
        }

        @Test
        public void andInsideOrWithAdditionalAnd() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .andWhere(
                            user.name.eq("Anna")
                                    .or(
                                            user.name.eq("Tom")
                                                    .and(user.id.eq(1L))
                                    )
                    )
                    .andWhere(user.id.between(100, 200));
            String jpql = serializer.serialize(query);
            assertEquals(
                    "select distinct user1 from User user1 where user1.id between ?1 and ?2 and (user1.name = ?3 or (user1.name = ?4 and user1.id = ?5))",
                    jpql
            );
            assertConstants(serializer, 100L, 200L, "Anna", "Tom", 1L);
        }

        @Test
        public void tripleNestedPredicates() {
            JoinerQuery<?, ?> query = Q.from(user)
                    .andWhere(
                            user.id.eq(1L)
                                    .or(
                                            user.name.eq("John")
                                                    .and(
                                                            user.id.gt(10)
                                                                    .or(user.id.lt(3))
                                                    )
                                    )
                    );
            String jpql = serializer.serialize(query);
            assertEquals(
                    "select distinct user1 from User user1 where user1.id = ?1 or (user1.name = ?2 and (user1.id > ?3 or user1.id < ?4))",
                    jpql
            );
            assertConstants(serializer, 1L, "John", 10L, 3L);
        }

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
        public void notInWithSingleConstant() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.notIn(0L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id <> ?1", jpql);
            assertConstants(serializer, 0L);
        }

        @Test
        public void notInWithMultipleConstants() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.notIn(0L, 1L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id not in ?1", jpql);
            assertConstants(serializer, List.of(0L, 1L));
        }

        @Test
        public void notInWithTwoParams() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.notIn(user.id, user.id));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id not in (user1.id, user1.id)", jpql);
            assertConstants(serializer);
        }

        @Test
        public void notInWithMultipleParams() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.notIn(user.id, user.id, user.id));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id not in ((user1.id, user1.id), user1.id)", jpql);
            assertConstants(serializer);
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
        public void leftJoinOn() {
            JoinerQuery<?, ?> query = Q.from(user).joins(J.left(user.groups).collectionPath(user.groups).on(group.name.isNotEmpty()));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 left join user1.groups group1 on not length(group1.name) = 0", jpql);
        }

        @Test
        public void leftJoinSingular() {
            JoinerQuery<?, ?> query = Q.from(user).joins(J.left(user.user).singularPath(user.user));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 left join fetch user1.user user", jpql);
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
            JoinerQuery<?, ?> query = Q.from(address)
                    .where(address.user.id.ne(Q.select(user1.id.max()).from(group).joins(J.inner(group.users))));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct address from Address address where address.user.id <> (select max(user1.id) from Group group1 inner join group1.users user1)", jpql);
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
        assertEquals("select distinct user1 from User user1 group by user1.name, user1.id", jpql);
    }

    @Test
    public void testHaving() {
        JoinerQuery<?, ?> query = Q.from(user).groupBy(user.name).having(user.id.gt(0L));
        String jpql = serializer.serialize(query);
        assertEquals("select distinct user1 from User user1 group by user1.name having user1.id > ?1", jpql);
        assertConstants(serializer, 0L);
    }

    @Nested
    class BasicProjections {

        @Test
        public void rootCustomAlias() {
            QUser myUser = new QUser("my_user");
            JoinerQuery<?, ?> query = Q.select(myUser.name).from(myUser);
            String jpql = serializer.serialize(query);
            assertEquals("select distinct my_user.name from User my_user", jpql);
            assertConstants(serializer);
        }

        @Test
        public void singleColumnProjection() {
            JoinerQuery<?, ?> query = Q.select(user.name).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1.name from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void singleJoinedColumnProjection() {
            JoinerQuery<?, ?> query = Q.select(group.name).from(user).joins(J.left(group).collectionPath(user.groups));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct group1.name from User user1 left join fetch user1.groups group1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void starEntityProjection() {
            JoinerQuery<?, ?> query = Q.from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void dtoMappingProjection() {
            JoinerQuery<?, ?> query = Q.select(ProjectionTest.class, user.name, user.id).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1.name, user1.id from User user1", jpql);
            assertConstants(serializer);
        }
    }

    @Nested
    class FunctionProjection {

        @Test
        public void testLowerFunction() {
            JoinerQuery<?, String> query = Q.select(user.name.lower()).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select lower(user1.name) from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void testSubstringFunction() {
            JoinerQuery<?, String> query = Q.select(user.name.substring(2)).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select substring(user1.name, ?1) from User user1", jpql);
            assertConstants(serializer, 2);
        }

        @Test
        public void testSubstring2Function() {
            JoinerQuery<?, String> query = Q.select(user.name.substring(0, 3)).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select substring(user1.name, ?1, ?2) from User user1", jpql);
            assertConstants(serializer, 0, 3);
        }

        @Test
        public void testCoalesceProjection() {
            JoinerQuery<?, ?> query = Q.select(user.salary.coalesce(user.id)).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select coalesce(user1.salary, user1.id) from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void testLengthFunction() {
            JoinerQuery<?, Integer> query = Q.select(user.name.length()).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select length(user1.name) from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void testConcatFunction() {
            JoinerQuery<?, String> query = Q.select(user.name.concat(user.name)).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select concat(user1.name, user1.name) from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void rootCountProjection() {
            JoinerQuery<?, ?> query = Q.count(user);
            String jpql = serializer.serialize(query);
            assertEquals("select count(user1) from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void columnCountProjection() {
            JoinerQuery<?, ?> query = Q.select(address.id.count()).from(address).groupBy(address.user);
            String jpql = serializer.serialize(query);
            assertEquals("select count(address.id) from Address address group by address.user", jpql);
        }

        @Test
        public void distinctColumnCountProjection() {
            JoinerQuery<?, ?> query = Q.select(address.id.countDistinct()).from(address).groupBy(address.user);
            String jpql = serializer.serialize(query);
            assertEquals("select count(distinct address.id) from Address address group by address.user", jpql);
        }

        @Test
        public void customCountProjection() {
            JoinerQuery<?, ?> query = Q.select(user.count()).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select count(user1) from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void distinctCustomCountProjection() {
            JoinerQuery<?, ?> query = Q.select(user.countDistinct()).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select count(distinct user1) from User user1", jpql);
            assertConstants(serializer);
        }

        @Test
        public void testAvgFunction() {
            JoinerQuery<?, ?> query = Q.select(address.id.avg()).from(address).groupBy(address.user);
            String jpql = serializer.serialize(query);
            assertEquals("select avg(address.id) from Address address group by address.user", jpql);
        }

        @Test
        public void testMaxFunction() {
            // Create a query with max function in a projection
            JoinerQuery<?, ?> query = Q.select(address.id.max()).from(address).groupBy(address.user);

            // Serialize the query
            String jpql = serializer.serialize(query);

            // Verify that the max function is correctly serialized
            assertEquals("select max(address.id) from Address address group by address.user", jpql);
        }

        @Test
        public void testMinFunction() {
            JoinerQuery<?, ?> query = Q.select(address.id.min()).from(address).groupBy(address.user);
            String jpql = serializer.serialize(query);
            assertEquals("select min(address.id) from Address address group by address.user", jpql);
        }

        @Test
        public void testSumFunction() {
            JoinerQuery<?, ?> query = Q.select(address.id.sum()).from(address).groupBy(address.user);
            String jpql = serializer.serialize(query);
            assertEquals("select sum(address.id) from Address address group by address.user", jpql);
        }

        @Test
        public void testFunctionInHaving() {
            // Create a query with a function in having clause
            JoinerQuery<?, ?> query = Q.select(address.id.avg())
                    .from(address)
                    .groupBy(address.user)
                    .having(address.id.count().gt(2));

            String jpql = serializer.serialize(query);

            assertEquals("select avg(address.id) from Address address group by address.user having count(address.id) > ?1", jpql);
            assertConstants(serializer, 2L);
        }
    }

    @Nested
    class SqlFunctions {

        @Test
        public void testLocateFunction() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.locate("oh").gt(0));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where locate(?1, user1.name) > ?2", jpql);
            assertConstants(serializer, "oh", 0);
        }

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
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.substring(1, 3).eq("oh"));
            String jpql = serializer.serialize(query);
            System.out.println("testSubstringFunction JPQL: " + jpql);
            assertEquals("select distinct user1 from User user1 where substring(user1.name, ?1, ?2) = ?3", jpql);
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
    class PredicateOperators {

        @Test
        public void testBetween() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.id.between(1L, 10L));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.id between ?1 and ?2", jpql);
            assertConstants(serializer, 1L, 10L);
        }

        @Test
        public void testCoalesceConstant() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.salary.coalesce(10).goe(11));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where coalesce(user1.salary, ?1) >= ?2", jpql);
            assertConstants(serializer, 10, 11);
        }

        @Test
        public void testCoalesceWithExpression() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.salary.coalesce(user.id).goe(11));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where coalesce(user1.salary, user1.id) >= ?1", jpql);
            assertConstants(serializer, 11);
        }

        @Test
        public void testIsEmpty() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.groups.isEmpty());
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.groups is empty", jpql);
            assertConstants(serializer);
        }

        @Test
        public void testIsNotEmpty() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.groups.isNotEmpty());
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where not user1.groups is empty", jpql);
            assertConstants(serializer);
        }

        @Test
        public void testAssociationSize() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.groups.size().gt(2));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where size(user1.groups) > ?1", jpql);
            assertConstants(serializer, 2);
        }

        @Test
        @Disabled
        public void testAssociationExists() {
            JoinerQuery<?, ?> query = Q.from(user).where(user1.groups.any().name.eq("test"));
            String jpql = serializer.serialize(query);
            assertEquals("select user1 from User user1 where exists (select 1 from user1.groups as user1_groups_0 where user1_groups_0.name = ?1)", jpql);
            assertConstants(serializer, 2);
        }

        @Test
        public void testCoalesce() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.coalesce("Unknown").eq("John"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where coalesce(user1.name, ?1) = ?2", jpql);
            assertConstants(serializer, "Unknown", "John");
        }

        @Test
        public void testNullif() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.name.nullif("Unknown").isNull());
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where nullif(user1.name, ?1) is null", jpql);
            assertConstants(serializer, "Unknown");
        }

        @Test
        public void testIndex() {
            JoinerQuery<?, ?> query = Q.from(user).where(user.groups.size().eq(1));
            String jpql = serializer.serialize(query);
            // TODO not supported yet
            assertConstants(serializer, 1);
        }

        @Test
        public void testCast() {
            JoinerQuery<?, ?> query = Q.select(user.id.castToNum(Integer.class)).from(user);
            String jpql = serializer.serialize(query);
            assertEquals("select user1.id numcast ?1 from User user1", jpql);
            assertConstants(serializer, Integer.class);
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

            assertEquals("select distinct user1 from User user1 where user1.id <> (select max(user1.id) from User user1)", jpql);
            assertConstants(serializer);
        }
    }

    @Nested
    class ComparisonOperators {
        @Test
        public void testEqualityPredicate() {
            JoinerQuery<?, ?> query = Q.from(user).andWhere(user.salary.eq(30));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.salary = ?1", jpql);
            assertConstants(serializer, 30);
        }

        @Test
        public void testNotEqualPredicate() {
            JoinerQuery<?, ?> query = Q.from(user).andWhere(user.name.ne("Alice"));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.name <> ?1", jpql);
            assertConstants(serializer, "Alice");
        }

        @Test
        public void testGreaterThanPredicate() {
            JoinerQuery<?, ?> query = Q.from(user).andWhere(user.salary.gt(50000));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.salary > ?1", jpql);
            assertConstants(serializer, 50000);
        }

        @Test
        public void testLessThanOrEqualPredicate() {
            JoinerQuery<?, ?> query = Q.from(user).andWhere(user.salary.loe(65));
            String jpql = serializer.serialize(query);
            assertEquals("select distinct user1 from User user1 where user1.salary <= ?1", jpql);
            assertConstants(serializer, 65);
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
