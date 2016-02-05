package cz.encircled.joiner.alias;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;

// TODO support for multiple wheres for some collection (i.e. user1.name, user2.name)
/**
 * This class is responsible for uniqueness of aliases in queries
 *
 * @author Kisel on 26.01.2016.
 */
public interface JoinerAliasResolver {

    EntityPath<?> resolveAlias(Path<?> path);

}
