package cz.encircled.joiner.test.config.hint;

import java.lang.reflect.Field;
import java.util.Collection;

import com.google.common.collect.Multimap;
import com.mysema.query.jpa.impl.AbstractJPAQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.QueryPostProcessor;
import cz.encircled.joiner.test.core.TestException;
import org.springframework.util.ReflectionUtils;

/**
 * @author Kisel on 04.02.2016.
 */
public class HintPostProcessor implements QueryPostProcessor {

    @Override
    @SuppressWarnings("unchecked")
    public void process(Q<?> request, JPAQuery query) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.makeAccessible(f);
        Multimap<String, Object> field = (Multimap<String, Object>) ReflectionUtils.getField(f, query);

        Object value = ((Collection) field.get("testHint")).iterator().next();
        if (!"testHintValue".equals(value)) {
            throw new TestException();
        }
    }

}
