package cz.encircled.joiner.test.config.property;

import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.QueryPostProcessor;
import cz.encircled.joiner.test.core.TestException;

/**
 * @author Kisel on 05.02.2016.
 */
public class PropertyPostProcessor implements QueryPostProcessor {

    @Override
    @SuppressWarnings("unchecked")
    public void process(Q<?> request, JPAQuery query) {
        if (!request.getCustomProperties().get("testProperty").equals("testPropertyValue")) {
            throw new TestException();
        }
    }

}
