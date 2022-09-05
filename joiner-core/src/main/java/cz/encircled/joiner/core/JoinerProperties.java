package cz.encircled.joiner.core;

import cz.encircled.joiner.query.QueryFeature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JoinerProperties {

    public boolean useStatelessSessions = false;

    public List<QueryFeature> defaultFeatures = new ArrayList<>();

    public Map<String, List<Object>> defaultHints = new LinkedHashMap<>();

    /**
     * If true, Hibernate StatelessSession will be used for all queries
     */
    public JoinerProperties setUseStatelessSessions(boolean useStatelessSessions) {
        this.useStatelessSessions = useStatelessSessions;
        return this;
    }

    /**
     * Add a given <code>feature</code> to all queries
     *
     * @see QueryFeature
     */
    public JoinerProperties addDefaultQueryFeature(QueryFeature feature) {
        defaultFeatures.add(feature);
        return this;
    }

    public void removeDefaultQueryFeature(QueryFeature feature) {
        defaultFeatures.remove(feature);
    }

    /**
     * Add a given JPA hint to all queries
     */
    public JoinerProperties addDefaultHint(String key, Object value) {
        if (!defaultHints.containsKey(key)) {
            defaultHints.put(key, new ArrayList<>());
        }
        defaultHints.get(key).add(value);
        return this;
    }

    public void removeDefaultHint(String key) {
        defaultHints.remove(key);
    }

}
