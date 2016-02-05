package cz.encircled.joiner.test.config;

import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.QueryPostProcessor;
import cz.encircled.joiner.test.core.TestException;

/**
 * @author Kisel on 01.02.2016.
 */
public class TestPostProcessor implements QueryPostProcessor {

    @Override
    public void process(Q<?> request, JPAQuery query) {
        throw new TestException();
    }

}
