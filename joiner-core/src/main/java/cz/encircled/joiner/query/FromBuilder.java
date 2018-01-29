package cz.encircled.joiner.query;

import com.querydsl.core.types.EntityPath;

/**
 * @author Kisel on 13.9.2016.
 */
public interface FromBuilder<R> {

    <T> JoinerQuery<T, R> from(EntityPath<T> from);

}
