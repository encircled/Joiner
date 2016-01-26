package cz.encircled.joiner.alias;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;

/**
 * @author Kisel on 26.01.2016.
 */
public interface JoinerAliasResolver {

    EntityPath<?> resolveAlias(Path<?> path);

}
