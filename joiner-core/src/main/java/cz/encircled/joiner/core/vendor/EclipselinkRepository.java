package cz.encircled.joiner.core.vendor;

import com.querydsl.core.JoinType;
import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.ExtendedJPAQuery;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.MultiValueMap;
import cz.encircled.joiner.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author Vlad on 13-Sep-16.
 */
public class EclipselinkRepository extends AbstractVendorRepository implements JoinerVendorRepository {

    private static final int MAX_NESTED_JOIN_DEPTH = 7;
    private static final String DOT_ESCAPED = "\\.";

    @Override
    public <R> ExtendedJPAQuery<R> createQuery(EntityManager entityManager) {
        JPAQuery<R> query = new JPAQuery<>(entityManager, EclipseLinkTemplates.DEFAULT);
        makeInsertionOrderHints(query);
        return new ExtendedJPAQuery<>(entityManager, query);
    }

    private void makeInsertionOrderHints(AbstractJPAQuery<?, ?> sourceQuery) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.setField(f, sourceQuery, new MultiValueMap<>());
    }

    @Override
    public void addFetch(JPAQuery<?> query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath) {
        String rootEntityAlias = rootPath.getMetadata().getName();
        String path = resolvePathToFieldFromRoot(rootEntityAlias, joinDescription, joins);

        String fetchHint = joinDescription.getJoinType().equals(JoinType.LEFTJOIN) ? "eclipselink.left-join-fetch" : "eclipselink.join-fetch";
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

