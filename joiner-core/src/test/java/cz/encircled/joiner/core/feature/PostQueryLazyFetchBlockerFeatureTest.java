package cz.encircled.joiner.core.feature;

import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.feature.PostQueryLazyFetchBlockerFeature;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Test;

import java.util.*;

import static cz.encircled.joiner.model.QAddress.address;
import static cz.encircled.joiner.model.QGroup.group;
import static cz.encircled.joiner.model.QUser.user1;
import static org.junit.jupiter.api.Assertions.*;

public abstract class PostQueryLazyFetchBlockerFeatureTest extends AbstractTest {

    @Test
    public void testLazyIsBlocked() {
        User user = joiner.findOne(Q.from(user1)
                .useStatelessSession()
                .addFeatures(new PostQueryLazyFetchBlockerFeature(entityManager))
                .where(user1.name.eq("normalUser1")));

        assertTrue(isLoaded(user, "addresses"));
        assertTrue(isLoaded(user, "groups"));
        assertTrue(isLoaded(user, "user"));
        assertEquals(new ArrayList<>(), user.getGroups());
        assertEquals(new HashSet<>(), user.getAddresses());
        assertNull(user.getUser());
    }

    @Test
    public void testInitializedFieldsArePreserved() {
        User user = joiner.findOne(Q.from(user1)
                .useStatelessSession()
                .addFeatures(new PostQueryLazyFetchBlockerFeature(entityManager))
                .joins(address)
                .joins(group)
                .joins(new QUser("user"))
                .where(user1.name.eq("normalUser1")));

        assertTrue(isLoaded(user, "addresses"));
        assertTrue(isLoaded(user, "groups"));
        assertTrue(isLoaded(user, "user"));
        assertFalse(user.getGroups().isEmpty());
        assertFalse(user.getAddresses().isEmpty());
        assertNotNull(user.getUser());
    }

    @Test
    public void testMockTypes() {
        PostQueryLazyFetchBlockerFeature f = new PostQueryLazyFetchBlockerFeature(entityManager);
        assertEquals(new TreeSet<>(), f.createEmptyValue(SortedSet.class));
        assertEquals(new TreeSet<>(), f.createEmptyValue(TreeSet.class));
        assertEquals(new TreeMap<>(), f.createEmptyValue(SortedMap.class));
        assertEquals(new HashMap<>(), f.createEmptyValue(Map.class));
        assertEquals(new HashMap<>(), f.createEmptyValue(HashMap.class));
        assertEquals(new ArrayList<>(), f.createEmptyValue(ArrayList.class));
        assertEquals(new HashSet<>(), f.createEmptyValue(HashSet.class));
        assertEquals(new LinkedList<>(), f.createEmptyValue(LinkedList.class));
    }

}
