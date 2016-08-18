package cz.encircled.joiner.core;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * @author Vlad on 16-Aug-16.
 */
public interface AliasResolver {

    void resolveJoinAlias(JoinDescription join, EntityPath<?> root);

}
