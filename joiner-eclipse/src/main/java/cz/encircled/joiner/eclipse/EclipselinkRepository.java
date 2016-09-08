package cz.encircled.joiner.eclipse;

import com.google.common.collect.ArrayListMultimap;
import com.mysema.query.JoinType;
import com.mysema.query.jpa.EclipseLinkTemplates;
import com.mysema.query.jpa.impl.AbstractJPAQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.core.vendor.AbstractVendorRepository;
import cz.encircled.joiner.core.vendor.JoinerVendorRepository;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author Kisel on 28.01.2016.
 */
public class EclipselinkRepository extends AbstractVendorRepository implements JoinerVendorRepository {

    private static final int MAX_NESTED_JOIN_DEPTH = 5;
    private static final String DOT_ESCAPED = "\\.";

    @Override
    public JPAQuery createQuery(EntityManager entityManager) {
        JPAQuery query = new JPAQuery(entityManager, EclipseLinkTemplates.DEFAULT);
        makeInsertionOrderHints(query);
        return query;
    }

    private void makeInsertionOrderHints(AbstractJPAQuery<JPAQuery> sourceQuery) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.setField(f, sourceQuery, ArrayListMultimap.create());
    }

    @Override
    public void addFetch(JPAQuery query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath) {
        String rootEntityAlias = rootPath.getMetadata().getName();
        String path = resolvePathToFieldFromRoot(rootEntityAlias, joinDescription, joins);

        String fetchHint = joinDescription.getJoinType().equals(com.mysema.query.JoinType.LEFTJOIN) ? "eclipselink.left-join-fetch" : "eclipselink.join-fetch";
        query.setHint(fetchHint, path);
    }

    @Override
    public void addJoin(JPAQuery query, JoinDescription joinDescription) {
        if (joinDescription.getJoinType().equals(JoinType.RIGHTJOIN)) {
            throw new JoinerException("Right join is not supported in EclipseLink!");
        }

        super.addJoin(query, joinDescription);
    }

    private String resolvePathToFieldFromRoot(String rootAlias, JoinDescription targetJoinDescription, Collection<JoinDescription> joins) {
        // Contains two elements: current attribute and it's parent (i.e. 'group' and 'users' for "group.users")
        String[] holder;

        if (targetJoinDescription.getCollectionPath() != null) {
            holder = targetJoinDescription.getCollectionPath().toString().split(DOT_ESCAPED);
        } else if (targetJoinDescription.getSinglePath() != null) {
            holder = targetJoinDescription.getSinglePath().toString().split(DOT_ESCAPED);
        } else {
            return null;
        }

        String parentSimpleName = holder[0];
        String result = holder[1];

        int i = 0;
        while ((holder = findJoinByEntitySimpleName(parentSimpleName, joins)) != null && i++ < MAX_NESTED_JOIN_DEPTH) {
            parentSimpleName = holder[0];
            result = holder[1] + "." + result;
        }

        return rootAlias + "." + result;
    }

    private String[] findJoinByEntitySimpleName(String targetAlias, Collection<JoinDescription> joins) {
        String[] result = null;
        for (JoinDescription join : joins) {
            String candidate = join.getAlias().getMetadata().getName();

            if (candidate.equals(targetAlias)) {
                if (join.getCollectionPath() != null) {
                    result = join.getCollectionPath().toString().split(DOT_ESCAPED);
                } else if (join.getSinglePath() != null) {
                    result = join.getSinglePath().toString().split(DOT_ESCAPED);
                }
                break;
            }
        }
        return result;
    }

}
