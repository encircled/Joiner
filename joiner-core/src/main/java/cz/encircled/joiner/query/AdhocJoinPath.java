package cz.encircled.joiner.query;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.EntityPathBase;

/**
 * Mock path implementation used for unmapped (adhoc) joins
 */
public class AdhocJoinPath<T> extends EntityPathBase<T> {
    public final EntityPath<?> target;

    public AdhocJoinPath(EntityPath<?> target) {
        super((Class<T>) target.getType(), target.getMetadata());
        this.target = target;
    }

}
