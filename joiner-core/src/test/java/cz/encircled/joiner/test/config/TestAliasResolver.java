package cz.encircled.joiner.test.config;

import java.util.HashMap;
import java.util.Map;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import cz.encircled.joiner.alias.JoinerAliasResolver;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QStatusType;
import cz.encircled.joiner.test.model.QUser;

/**
 * @author Kisel on 26.01.2016.
 */
public class TestAliasResolver implements JoinerAliasResolver {

    public static final QStatus STATUS_ON_GROUP = new QStatus("groupStatus");
    public static final QStatus STATUS_ON_USER = new QStatus("userStatus");
    public static final QStatusType STATUS_TYPE_ON_STATUS_ON_GROUP = new QStatusType("statusTypeOnStatusOnGroup");
    public static final QStatusType STATUS_TYPE_ON_STATUS_ON_USER = new QStatusType("statusTypeOnStatusOnUser");

    public static Map<Path<?>, EntityPath<?>> collections = new HashMap<>();

    static {
        collections.put(QGroup.group.statuses, STATUS_ON_GROUP);
        collections.put(QUser.user1.statuses, STATUS_ON_USER);
        collections.put(TestAliasResolver.STATUS_ON_GROUP.statusType, STATUS_TYPE_ON_STATUS_ON_GROUP);
        collections.put(TestAliasResolver.STATUS_ON_USER.statusType, STATUS_TYPE_ON_STATUS_ON_USER);
    }

    public EntityPath<?> resolveAlias(Path<?> path) {
        return collections.get(path);
    }

}
