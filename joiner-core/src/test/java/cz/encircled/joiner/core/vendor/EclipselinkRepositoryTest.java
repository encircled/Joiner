package cz.encircled.joiner.core.vendor;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.QTuple;
import cz.encircled.joiner.model.QUser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EclipselinkRepositoryTest {

    EclipselinkRepository el = new EclipselinkRepository();

    @Test
    @SuppressWarnings("unchecked")
    public void testTransformToTupleWithSingleParam() {
        List<Tuple> objects = (List<Tuple>) el.transformToTuple(new TestTuple(QUser.user1.id, QUser.user1.name), List.of(2L));
        assertEquals(1, objects.size());
        assertEquals(2L, objects.get(0).get(QUser.user1.id));
        assertEquals(1, objects.get(0).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTransformToTupleWithMissingParam() {
        List<Tuple> objects = (List<Tuple>) el.transformToTuple(new TestTuple(QUser.user1.id, QUser.user1.name), singletonList(new Object[]{2L}));
        assertEquals(1, objects.size());
        assertEquals(2L, objects.get(0).get(QUser.user1.id));
        assertEquals(1, objects.get(0).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTransformToTupleAllParams() {
        List<Tuple> objects = (List<Tuple>) el.transformToTuple(new TestTuple(QUser.user1.id, QUser.user1.name), singletonList(new Object[]{2L, "name"}));
        assertEquals(1, objects.size());
        assertEquals(2L, objects.get(0).get(QUser.user1.id));
        assertEquals("name", objects.get(0).get(QUser.user1.name));
        assertEquals(2, objects.get(0).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTransformToTupleFromNull() {
        ArrayList<Object> o = new ArrayList<>();
        o.add(null);
        List<Tuple> objects = (List<Tuple>) el.transformToTuple(new TestTuple(QUser.user1.id, QUser.user1.name), o);
        assertEquals(1, objects.size());
        assertEquals(null, objects.get(0).get(QUser.user1.id));
        assertEquals(1, objects.get(0).size());
    }

    class TestTuple extends QTuple {
        TestTuple(Expression<?>... args) {
            super(args);
        }
    }

}
