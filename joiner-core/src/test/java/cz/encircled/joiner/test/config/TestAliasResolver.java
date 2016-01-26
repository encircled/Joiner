package cz.encircled.joiner.test.config;

import java.util.HashMap;
import java.util.Map;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import cz.encircled.joiner.alias.JoinerAliasResolver;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QUser;

/**
 * @author Kisel on 26.01.2016.
 */
public class TestAliasResolver implements JoinerAliasResolver {

    public static Map<Path<?>, EntityPath<?>> collections = new HashMap<Path<?>, EntityPath<?>>();

    static {
        collections.put(QGroup.group.statuses, new QStatus("groupStatus"));
        collections.put(QUser.user.statuses, new QStatus("userStatus"));
    }

    public EntityPath<?> resolveAlias(Path<?> path) {
        return collections.get(path);
    }
}
