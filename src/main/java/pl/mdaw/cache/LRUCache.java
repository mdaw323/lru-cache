package pl.mdaw.cache;

import lombok.SneakyThrows;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

public class LRUCache extends AbstractValueAdaptingCache {

    private final Map<Object, Object> internalMap;
    private final long ttl; //in milliseconds
    private final String name;

    public LRUCache(String name, int maximumCapacity) {
        this(name, maximumCapacity, 0L);
    }

    public LRUCache(String name, int maximumCapacity, long ttl) {
        this(name, Math.min(256,maximumCapacity), maximumCapacity, ttl);
    }

    public LRUCache(String name, int initialCapacity, int maximumCapacity) {
        this(name, initialCapacity, maximumCapacity, 0L);
    }

    public LRUCache(String name, int initialCapacity, int maximumCapacity, long ttl) {
        super(false);
        this.ttl = ttl;
        this.name = name;
        this.internalMap = Collections.synchronizedMap(new LRULinkedHashMap<>(initialCapacity, maximumCapacity));
    }

    @Override protected Object lookup(Object key) {
        return this.internalMap.get(key);
    }

    @Override public String getName() {
        return name;
    }

    @Override public Object getNativeCache() {
        return internalMap;
    }

    @SuppressWarnings({"unchecked"})
    @Nullable @Override public <T> T get(Object key, Callable<T> valueLoader) {
        return (T) fromStoreValue(this.internalMap.computeIfAbsent(key, k -> LRUCache.this.toStoreValue(safelyCall(valueLoader))));
    }

    @SneakyThrows
    private <T> T safelyCall(Callable<T> valueLoader) {
        return valueLoader.call();
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        this.internalMap.put(key, toStoreValue(value));
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        Object existing = this.internalMap.putIfAbsent(key, toStoreValue(value));
        return toValueWrapper(existing);
    }

    @Override
    public void evict(Object key) {
        this.internalMap.remove(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return (this.internalMap.remove(key) != null);
    }

    @Override
    public void clear() {
        this.internalMap.clear();
    }

    @Override
    public boolean invalidate() {
        boolean notEmpty = !this.internalMap.isEmpty();
        this.internalMap.clear();
        return notEmpty;
    }

    @Override
    protected Object toStoreValue(@Nullable Object userValue) {
        Object storeValue = super.toStoreValue(userValue);
        return this.ttl > 0L ? new ExpiringValue(storeValue, this.ttl) : storeValue;
    }

    @Override
    protected Object fromStoreValue(@Nullable Object storeValue) {
        if (storeValue != null && this.ttl > 0L) {
            ExpiringValue expiringValue = (ExpiringValue) storeValue;
            return super.fromStoreValue(expiringValue.getNotExpiredValue());
        } else {
            return super.fromStoreValue(storeValue);
        }
    }

    private static class ExpiringValue {
        private final Object value;
        private final long expiryTime;

        ExpiringValue(Object value, long ttl) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttl;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        @Nullable Object getNotExpiredValue() {
            return isExpired() ? null : value;
        }
    }
}
