package cz.encircled.joiner.test.core.resolver;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import cz.encircled.joiner.core.DefaultPredicateAliasResolver;
import cz.encircled.joiner.core.PredicateAliasResolver;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.core.AbstractTest;
import cz.encircled.joiner.test.model.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Vlad on 04-Aug-16.
 */
@ContextConfiguration(classes = {TestConfig.class})
public class PredicateResolverTest extends AbstractTest {

    private PredicateAliasResolver resolver = new DefaultPredicateAliasResolver();

    private QStatus testStatus = new QStatus("testStatus");

    @Test
    public void testOperationResolved() {
        Map<AnnotatedElement, List<JoinDescription>> grouped = Stream.of(J.left(testStatus))
                .collect(Collectors.groupingBy(j -> j.getOriginalAlias().getAnnotatedElement()));
        Path<String> resolved = resolver.resolvePath(QStatus.status.name, grouped, Collections.emptySet());
        Assert.assertEquals(testStatus.name, resolved);
    }

    @Test
    public void testPredicateResolved() {
        Predicate resolved = resolver.resolvePredicate(QStatus.status.id.eq(1L), Collections.singletonList(J.left(testStatus)), Collections.emptySet());
        Assert.assertEquals(testStatus.id.eq(1L), resolved);
    }

    @Test
    public void testMissingAliasNotChanged() {
        Predicate resolved = resolver.resolvePredicate(QStatus.status.id.eq(1L), Collections.singletonList(J.left(QUser.user1)), Collections.emptySet());
        Assert.assertEquals(QStatus.status.id.eq(1L), resolved);
    }

    @Test
    public void testCustomMissingAliasNotChanged() {
        Predicate resolved = resolver.resolvePredicate(new QStatus("some").id.eq(1L), Collections.singletonList(J.left(QUser.user1)), Collections.emptySet());
        Assert.assertEquals(new QStatus("some").id.eq(1L), resolved);
    }

    @Test
    public void testPresentAliasNotChanged() {
        Predicate resolved = resolver.resolvePredicate(new QStatus("notChanged").id.eq(1L), Collections.singletonList(J.left(testStatus)), Collections.singleton(new QStatus("notChanged")));
        Assert.assertEquals("notChanged.id = 1", resolved.toString());
    }

    @Test
    public void testAmbiguousJoinsNotChanged() {
        Predicate resolved = resolver.resolvePredicate(new QStatus("notChanged").id.eq(1L), Arrays.asList(J.left(testStatus), J.left(new QStatus("anotherStatus"))),
                Collections.emptySet());
        Assert.assertEquals("notChanged.id = 1", resolved.toString());
    }

    @Test
    public void testWhereResolvedInQuery() {
        List<Group> result = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QUser.user1).nested(QStatus.status))
                .where(QStatus.status.id.lt(2000L).and(QStatus.status.id.ne(3L))
                        .or(QStatus.status.id.eq(3L).and(QStatus.status.id.ne(3L))).and(QStatus.status.id.ne(4L))));

        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testNumberPathArgWhereResolvedInQuery() {
        List<Group> result = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QUser.user1).nested(QStatus.status))
                .where(QStatus.status.id.gt(0L)));

        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleArgWhereResolvedInQuery() {
        List<Group> result = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QUser.user1).nested(QStatus.status))
                .where(QStatus.status.id.isNotNull()));

        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testNestedSingleArgWhereResolvedInQuery() {
        List<Group> result = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QUser.user1).nested(QStatus.status))
                .where(QStatus.status.id.gt(100L).or(QStatus.status.id.isNotNull())));

        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testManyToOneResolvedInQuery() {
        List<Status> statuses = joiner.find(Q.from(QStatus.status)
                .joins(J.left(QUser.user1).nested(QGroup.group))
                .where(QGroup.group.id.gt(100L).or(QGroup.group.id.isNotNull())));

        Assert.assertFalse(statuses.isEmpty());
    }

    @Test
    @Ignore // maybe later?
    public void testFromResolvedInQuery() {
        joiner.find(Q.from(new QGroup("some")).where(QGroup.group.id.isNotNull()));
    }

}
