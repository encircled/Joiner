package cz.encircled.joiner.test.config;

import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.repository.QueryPostProcessor;

/**
 * @author Kisel on 01.02.2016.
 */
public class TestPostProcessor implements QueryPostProcessor {

    @Override
    public void process(JPAQuery query) {
        throw new NullPointerException("TestPostProcessor");
    }

}
