package cz.encircled.joiner.test.config;

import java.lang.reflect.Field;

import com.google.common.collect.Multimap;
import com.mysema.query.jpa.impl.AbstractJPAQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.repository.QueryPostProcessor;
import org.junit.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author Kisel on 04.02.2016.
 */
public class HintPostProcessor implements QueryPostProcessor {

    @Override
    @SuppressWarnings("unchecked")
    public void process(JPAQuery query) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.makeAccessible(f);
        Multimap<String, Object> field = (Multimap<String, Object>) ReflectionUtils.getField(f, query);
        Assert.assertNotEquals("testHintValue", field.get("testHint"));
    }

}
