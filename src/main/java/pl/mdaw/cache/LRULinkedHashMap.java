package pl.mdaw.cache;

import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class LRULinkedHashMap<K,V> extends LinkedHashMap<K, V> {
    // skopiowane z HashMap
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    private final int maximumCapacity;

    public LRULinkedHashMap(int initialCapacity, float loadFactor, int maximumCapacity) {
        super(initialCapacity, loadFactor, true);
        this.maximumCapacity = maximumCapacity;
    }

    public LRULinkedHashMap(int initialCapacity, int maximumCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, maximumCapacity);
    }

    @Override protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maximumCapacity;
    }
}
