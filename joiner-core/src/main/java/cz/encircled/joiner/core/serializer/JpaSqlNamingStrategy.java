package cz.encircled.joiner.core.serializer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;

import java.lang.reflect.AnnotatedElement;

public class JpaSqlNamingStrategy implements SqlNamingStrategy {

    private final Metamodel metamodel;

    public JpaSqlNamingStrategy(Metamodel metamodel) {
        this.metamodel = metamodel;
    }

    @Override
    public String getTableName(Class<?> entityType) {
        Table tableAnn = entityType.getAnnotation(Table.class);
        if (tableAnn != null && !tableAnn.name().isEmpty()) {
            return tableAnn.name();
        }
        Entity entityAnn = entityType.getAnnotation(Entity.class);
        if (entityAnn != null && !entityAnn.name().isEmpty()) {
            // often not the real physical table, but good fallback
            return entityAnn.name();
        }
        // last resort: unqualified simple class name
        return entityType.getSimpleName();
    }

    @Override
    public String toSql(Path<?> path) {
        PathMetadata md = path.getMetadata();
        Path<?> parent = md.getParent();

        // root path -> alias, e.g. "u"
        if (parent == null) {
            return md.getName();
        }

        // parent is another path, usually entity alias
        String parentSql = toSql(parent); // recursion ends at root alias
        String attrName = md.getName();

        // try to resolve physical column name via JPA metamodel
        try {
            EntityType<?> entityType = metamodel.entity(parent.getType());
            if (entityType != null && entityType.getAttribute(attrName) instanceof SingularAttribute<?, ?> singular) {
                AnnotatedElement member = (AnnotatedElement) singular.getJavaMember();
                Column col = member.getAnnotation(Column.class);
                String columnName;
                if (col != null && !col.name().isEmpty()) {
                    columnName = col.name();
                } else {
                    // you could also plug in Hibernate's NamingStrategy here
                    columnName = attrName;
                }
                return parentSql + "." + columnName;
            }
        } catch (IllegalArgumentException ex) {
            // parent type not an entity, fall back
        }

        // fallback: parentAlias + "." + attrName
        return parentSql + "." + attrName;
    }
}
