package cz.encircled.joiner.core;

import com.querydsl.core.Tuple;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cz.encircled.joiner.model.QUser.user1;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class ResultProjectionTest extends AbstractTest {

    @Test
    public void subMinMaxProjection() {
        Long maxResult = joiner.findOne(Q.select(user1.id.max()).from(user1));
        Long minResult = joiner.findOne(Q.select(user1.id.min()).from(user1));

        Long maxActual = (Long) entityManager.createQuery("select max(u.id) from User u").getSingleResult();
        Long minActual = (Long) entityManager.createQuery("select min(u.id) from User u").getSingleResult();

        assertEquals(maxActual, maxResult);
        assertEquals(minActual, minResult);
    }

    @Test
    public void singleProjection() {
        List<String> tuple = joiner.find(Q.select(user1.name).from(user1).desc(user1.name).where(user1.name.in("user1", "superUser1")));
        assertEquals(2, tuple.size());
        assertEquals("user1", tuple.get(0));
        assertEquals("superUser1", tuple.get(1));
    }

    @Test
    public void tupleProjection() {
        List<Tuple> tuple = joiner.find(Q.select(user1.id, user1.name).from(user1).desc(user1.name).where(user1.name.in("user1", "superUser1")));
        assertEquals(2, tuple.size());
        assertEquals("user1", tuple.get(0).get(user1.name));
        assertEquals("superUser1", tuple.get(1).get(user1.name));
    }

    @Test
    public void tupleProjectionFromJoin() {
        List<Tuple> tuple = joiner.find(Q.select(user1.id, user1.name).from(QGroup.group).desc(user1.name).joins(J.inner(user1)).where(user1.name.in("user1", "superUser1")));
        assertEquals(2, tuple.size());
        assertEquals("user1", tuple.get(0).get(user1.name));
        assertEquals("superUser1", tuple.get(1).get(user1.name));
    }

}
