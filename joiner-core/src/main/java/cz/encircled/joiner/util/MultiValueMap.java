package cz.encircled.joiner.util;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MultiValueMap<K, V> implements Map<K, V> {

    ArrayListValuedHashMap<K, V> delegate = new ArrayListValuedHashMap<>();

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return (V) delegate.get((K) key);
    }

    @Override
    public V put(K key, V value) {
        delegate.put(key, value);
        return value;
    }

    @Override
    public V remove(Object key) {
        delegate.remove(key);
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new LinkedHashSet<>(delegate.entries());
    }

}
