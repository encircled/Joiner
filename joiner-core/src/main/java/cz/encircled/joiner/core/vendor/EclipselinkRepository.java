package cz.encircled.joiner.core.vendor;

import com.querydsl.core.JoinType;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.core.JoinerJPQLSerializer;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.MultiValueMap;
import cz.encircled.joiner.util.ReflectionUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Vlad on 13-Sep-16.
 */
public class EclipselinkRepository extends AbstractVendorRepository implements JoinerVendorRepository {

    private static final int MAX_NESTED_JOIN_DEPTH = 7;
    private static final String DOT_ESCAPED = "\\.";

    @Override
    public <R> JPQLQuery<R> createQuery(JoinerQuery<?, R> request, EntityManager entityManager, JoinerProperties joinerProperties) {
        if (request.isStatelessSession() == Boolean.TRUE || joinerProperties.useStatelessSessions) {
            throw new IllegalStateException("StatelessSession is not supported by Eclipselink!");
        }
        JPAQuery<R> query = new JPAQuery<>(entityManager, EclipseLinkTemplates.DEFAULT);
        makeInsertionOrderHints(query);
        return query;
    }

    private void makeInsertionOrderHints(AbstractJPAQuery<?, ?> sourceQuery) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.setField(f, sourceQuery, new MultiValueMap<>());
    }

    @Override
    public void addFetch(JPQLQuery<?> query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath) {
        String rootEntityAlias = rootPath.getMetadata().getName();
        String path = resolvePathToFieldFromRoot(rootEntityAlias, joinDescription, joins);

        String fetchHint = joinDescription.getJoinType().equals(JoinType.LEFTJOIN) ? "eclipselink.left-join-fetch" : "eclipselink.join-fetch";
        ((AbstractJPAQuery<?, ?>) query).setHint(fetchHint, path);
    }

    @Override
    public void addJoin(JPQLQuery<?> query, JoinDescription joinDescription) {
        if (joinDescription.getJoinType().equals(JoinType.RIGHTJOIN)) {
            throw new JoinerException("Right join is not supported in EclipseLink!");
        }

        super.addJoin(query, joinDescription);
    }

    public <T> List<T> getResultList(JoinerQuery<?, T> request, JoinerProperties joinerProperties, EntityManager entityManager) {
        JoinerJPQLSerializer serializer = new JoinerJPQLSerializer();
        String queryString = serializer.serialize(request, request.isCount());
        System.out.println("\nJoiner:\n" + queryString + "\n");

        Query jpaQuery = entityManager.createQuery(queryString);

        setQueryParams(serializer, jpaQuery, request);

        Expression<T> projection = request.getReturnProjection();
        if (projection instanceof FactoryExpression) {
            FactoryExpression fe = (FactoryExpression) projection;
            List<?> results = jpaQuery.getResultList();
            List<Object> rv = new ArrayList(results.size());

            for (Object o : results) {
                if (o != null) {
                    if (!o.getClass().isArray()) {
                        o = new Object[]{o};
                    }

                    rv.add(fe.newInstance((Object[]) o));
                } else {
                    rv.add(fe.newInstance(new Object[]{null}));
                }
            }

            return (List<T>) rv;
        }

        return jpaQuery.getResultList();
    }

    private String resolvePathToFieldFromRoot(String rootAlias, JoinDescription targetJoinDescription, Collection<JoinDescription> joins) {
        // Contains two elements: current attribute and it's parent (i.e. 'group' and 'users' for "group.users")
        String[] holder;

        if (targetJoinDescription.getCollectionPath() != null) {
            holder = targetJoinDescription.getCollectionPath().toString().split(DOT_ESCAPED);
        } else if (targetJoinDescription.getSingularPath() != null) {
            holder = targetJoinDescription.getSingularPath().toString().split(DOT_ESCAPED);
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
                } else if (join.getSingularPath() != null) {
                    result = join.getSingularPath().toString().split(DOT_ESCAPED);
                }
                break;
            }
        }
        return result;
    }

}

