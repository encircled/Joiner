package cz.encircled.joiner.repository;

import com.mysema.query.jpa.impl.JPAQuery;

/**
 * @author Kisel on 01.02.2016.
 */
public interface QueryPostProcessor {

    void process(JPAQuery query);

}
