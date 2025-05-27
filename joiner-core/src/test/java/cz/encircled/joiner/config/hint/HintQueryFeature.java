package cz.encircled.joiner.config.hint;

import cz.encircled.joiner.core.TestException;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryFeature;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.Query;

import java.util.Map;

/**
 * @author Kisel on 04.02.2016.
 */
public class HintQueryFeature implements QueryFeature {

    public static String TEST_HINT = "jakarta.persistence.cache.storeMode";
    public static String TEST_HINT_VALUE = CacheStoreMode.BYPASS.toString();
    public static String TEST_ERROR_HINT_VALUE = CacheStoreMode.USE.toString();

    @Override
    public <T, R> Query after(JoinerQuery<T, R> request, Query query) {
        Map<String, Object> hints = query.getHints();

        Object h = hints != null ? hints.get(TEST_HINT) : null;
        if (!TEST_HINT_VALUE.equals(h != null ? h.toString() : null)) {
            throw new TestException();
        }

        return query;
    }
}
