package cz.encircled.joiner.test.config.hint;

import com.google.common.collect.Multimap;
import com.mysema.query.jpa.impl.AbstractJPAQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.QueryFeature;
import cz.encircled.joiner.test.core.TestException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author Kisel on 04.02.2016.
 */
public class HintQueryFeature implements QueryFeature {

    @Override
    public <T> Q<T> before(Q<T> request) {
        return request;
    }

    @Override
    public JPAQuery after(Q<?> request, JPAQuery query) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.makeAccessible(f);
        Multimap<String, Object> field = (Multimap<String, Object>) ReflectionUtils.getField(f, query);

        Object value = ((Collection) field.get("testHint")).iterator().next();
        if (!"testHintValue".equals(value)) {
            throw new TestException();
        }

        return query;
    }

}