package cz.encircled.joiner.util;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MultiValueMapTest {

    @Test
    public void testSize() {
        MultiValueMap<Integer, Integer> map = new MultiValueMap<>();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());

        map.put(1, 1);
        map.put(1, 2);
        map.put(1, 3);
        assertEquals(3, map.size());
        assertFalse(map.isEmpty());
        map.put(2, 2);
        assertEquals(4, map.size());
        map.put(2, 3);
        assertEquals(5, map.size());

        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testContains() {
        MultiValueMap<Integer, Integer> map = new MultiValueMap<>();
        map.put(2, 1);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue(2));

        map.put(1, 2);
        assertTrue(map.containsKey(1));
        assertTrue(map.containsValue(2));

        map.remove(1);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue(2));
    }

    @Test
    public void testSets() {
        MultiValueMap<Integer, Integer> map = new MultiValueMap<>();
        map.put(3, 1);
        map.put(4, 2);

        Set<Integer> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains(3));
        assertTrue(keys.contains(4));

        Collection<Integer> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
    }

}
