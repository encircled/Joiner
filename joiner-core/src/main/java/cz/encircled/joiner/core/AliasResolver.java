package cz.encircled.joiner.core;

import com.querydsl.core.types.EntityPath;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * Implementation is responsible for aliases lookup in a query
 *
 * @author Vlad on 16-Aug-16.
 */
public interface AliasResolver {

    void resolveJoinAlias(JoinDescription join, EntityPath<?> root);

}
