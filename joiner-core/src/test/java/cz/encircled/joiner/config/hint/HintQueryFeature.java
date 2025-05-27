package cz.encircled.joiner.config.hint;

import com.querydsl.jpa.impl.AbstractJPAQuery;
import cz.encircled.joiner.core.TestException;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryFeature;
import cz.encircled.joiner.util.MultiValueMap;
import jakarta.persistence.Query;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author Kisel on 04.02.2016.
 */
public class HintQueryFeature implements QueryFeature {

    @Override
    public <T, R> JoinerQuery<T, R> before(final JoinerQuery<T, R> request) {
        return request;
    }

    @Override
    public <T, R> Query after(JoinerQuery<T, R> request, Query query) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.makeAccessible(f);
        MultiValueMap<String, Object> field = (MultiValueMap<String, Object>) ReflectionUtils.getField(f, query);

        Object value = ((Collection) field.get("testHint")).iterator().next();
        if (!"testHintValue".equals(value)) {
            throw new TestException();
        }

        return query;
    }
}
