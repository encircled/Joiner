package cz.encircled.joiner.repository;

import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.Q;

/**
 * @author Kisel on 01.02.2016.
 */
public interface QueryPostProcessor {

    void process(Q<?> request, JPAQuery query);

}
