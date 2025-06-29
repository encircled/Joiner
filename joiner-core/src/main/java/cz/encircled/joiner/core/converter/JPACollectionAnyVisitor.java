package cz.encircled.joiner.core.converter;

import com.querydsl.core.support.CollectionAnyVisitor;
import com.querydsl.core.support.Context;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import cz.encircled.joiner.query.CollectionJoinerQuery;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.J;
import jakarta.persistence.Entity;

import static cz.encircled.joiner.util.JoinerUtils.getQClass;

public class JPACollectionAnyVisitor extends CollectionAnyVisitor {

    @SuppressWarnings("unchecked")
    @Override
    public Predicate exists(Context c, Predicate condition) {
        JoinerQuery jq = null;
        for (int i = 0; i < c.paths.size(); i++) {
            Path<?> child = c.paths.get(i).getMetadata().getParent();
            EntityPath<Object> replacement = (EntityPath<Object>) c.replacements.get(i);
            Class<?> type = c.paths.get(i).getType();
            if (type.isAnnotationPresent(Entity.class)) {
                ListPath<Object, ?> listPath = Expressions.listPath((Class) type, (Class) getQClass(type), child.getMetadata());

                if (jq == null) {
                    jq = new CollectionJoinerQuery<>(listPath, replacement);
                } else {
                    jq.joins(J.inner(listPath).collectionPath(listPath).alias(replacement));
                }

            } else {
                throw new UnsupportedOperationException("Only JPA associations are allowed in any() queries");
            }
        }
        c.clear();

        if (jq == null) return condition;

        jq.where(condition);
        return ExpressionUtils.predicate(Ops.EXISTS, jq);
    }

}
