package cz.encircled.joiner.feature;

import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryFeature;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Metamodel;
import org.hibernate.Hibernate;
import org.hibernate.UnknownEntityTypeException;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.results.graph.FetchTimingAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Use this query feature to prevent uninitialized lazy attributes from being fetched when accessed.
 * Uninitialized attributes will be initialized with an empty collection or null for singular associations.
 * This works only with Hibernate stateless sessions, which can be enabled at the query level or globally using  {@link cz.encircled.joiner.core.JoinerProperties#setUseStatelessSessions})
 */
public class PostQueryLazyFetchBlockerFeature implements QueryFeature {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    MappingMetamodel mappingMetamodel;


    public PostQueryLazyFetchBlockerFeature(EntityManager entityManager) {
        Metamodel metamodel = entityManager.getMetamodel();
        if (metamodel instanceof MappingMetamodel) {
            mappingMetamodel = (MappingMetamodel) metamodel;
        } else {
            log.warn("Metamodel is not a MappingMetamodel, PostQueryLazyFetchBlockerFeature will not work");
        }
    }

    @Override
    public <T, R> void postLoad(JoinerQuery<T, R> request, List<R> result) {
        if (request.isStatelessSession() != Boolean.TRUE) {
            log.error("PostQueryLazyFetchBlockerFeature must be used in a Hibernate stateless session! Skipping...");
            return;
        }
        if (mappingMetamodel != null && result != null) {
            Set<Object> visited = new HashSet<>();
            for (R e : result) {
                blockLazyAttributes(e, visited);
            }
        }
    }

    void blockLazyAttributes(Object entity, Set<Object> visited) {
        if (entity instanceof Collection) {
            for (Object o : (Collection<?>) entity) {
                blockLazyAttributes(o, visited);
            }
        } else if (entity != null && visited.add(entity)) {
            try {
                EntityPersister entityDescriptor = mappingMetamodel.getEntityDescriptor(entity.getClass());
                String[] propertyNames = entityDescriptor.getPropertyNames();
                for (int i = 0; i < propertyNames.length; i++) {
                    AttributeMapping attributeMapping = entityDescriptor.getAttributeMapping(i);
                    if (attributeMapping instanceof FetchTimingAccess) {
                        if (((FetchTimingAccess) attributeMapping).getTiming() == FetchTiming.DELAYED) {
                            if (Hibernate.isInitialized(entityDescriptor.getPropertyValue(entity, propertyNames[i]))) { // isLoaded
                                blockLazyAttributes(entityDescriptor.getPropertyValue(entity, propertyNames[i]), visited);
                            } else {
                                entityDescriptor.setValue(entity, i, createEmptyValue(attributeMapping.getJavaType().getJavaType()));
                            }
                        }
                    }
                }
            } catch (UnknownEntityTypeException e) {
                System.out.println("Error for " + entity);
            }
        }
    }

    public Object createEmptyValue(Type javaType) {
        return switch (javaType.getTypeName()) {
            case "java.util.SortedSet", "java.util.TreeSet" -> new TreeSet<>();
            case "java.util.Set", "java.util.HashSet" -> new HashSet<>();
            case "java.util.List", "java.util.Collection", "java.util.LinkedList", "java.util.ArrayList" ->
                    new ArrayList<>();
            case "java.util.SortedMap" -> new TreeMap<>();
            case "java.util.Map", "java.util.HashMap" -> new HashMap<>();
            default -> null;
        };
    }

}
