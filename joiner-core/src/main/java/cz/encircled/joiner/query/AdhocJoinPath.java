package cz.encircled.joiner.query;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Visitor;

import java.lang.reflect.AnnotatedElement;

/**
 * Mock path implementation used for unmapped (adhoc) joins
 */
public class AdhocJoinPath implements EntityPath<Object> {
    public final EntityPath<?> target;

    public AdhocJoinPath(EntityPath<?> target) {
        this.target = target;
    }

    @Override
    public Object getMetadata(Path<?> property) {
        return target.getMetadata(property);
    }

    @Override
    public PathMetadata getMetadata() {
        return target.getMetadata();
    }

    @Override
    public Path<?> getRoot() {
        return target.getRoot();
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return target.getAnnotatedElement();
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return target.accept(v, context);
    }

    @Override
    public Class<?> getType() {
        return target.getType();
    }

}
