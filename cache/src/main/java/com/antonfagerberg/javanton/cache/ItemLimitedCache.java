package com.antonfagerberg.javanton.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

public non-sealed class ItemLimitedCache<K, V> implements Cache<K, V> {

    private record ValueStat<T>(T value, long accessed) {
    }

    private final HashMap<K, ValueStat<V>> items;
    private final TreeMap<Long, Set<K>> accessedKeys;
    private final int maxSize;

    /**
     * A cache that can only grow to a certain defined the parameter 'size'.
     * When the cache is full and a new item is added, the item accessed the fewest times will be discarded.
     * If several items have the same amount of times accessed, one of them will be discarded, no guarantee which.
     *
     * @param size max size of the cache.
     */
    public ItemLimitedCache(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        items = new HashMap<>(size);
        accessedKeys = new TreeMap<>();
        maxSize = size;
    }

    /**
     * Get the value associated with the key.
     * If the key has no value in the cache, null will be returned.
     * If a value is returned, the number of times this value has been accessed will increase by one.
     *
     * @param key the key.
     * @return value or null if it doesn't exist.
     */
    @Override
    public V get(K key) {
        final var valueStat = items.get(key);
        if (valueStat == null) {
            return null;
        }

        removeKey(valueStat.accessed(), key);
        setUsedKey(valueStat.accessed() + 1, key);
        items.put(key, new ValueStat<>(valueStat.value(), valueStat.accessed() + 1));
        return valueStat.value();
    }

    /**
     * Remove the value associated with the key.
     * Returns the associated value or null if the key did not exist.
     *
     * @param key the key.
     * @return the value associated with the deleted key, or null if it didn't exist.
     */
    @Override
    public V remove(K key) {
        final var valueStat = items.remove(key);
        if (valueStat == null) {
            return null;
        }

        removeKey(valueStat.accessed(), key);
        return valueStat.value();
    }

    /**
     * Put a new key-value mapping.
     * If the key already existed, the previous value will be returned, otherwise null.
     * The number of accesses for the value will be set to 0 independent of if a previous value existed or not.
     *
     * @param key   the key.
     * @param value the value.
     * @return the previous value associated with the key or null if it didn't exist.
     */
    @Override
    public V put(K key, V value) {
        if (items.size() == maxSize && !items.containsKey(key)) {
            final var usedKeysEntry = accessedKeys.firstEntry();
            final var keySet = usedKeysEntry.getValue();
            K firstKey = keySet.iterator().next();
            keySet.remove(firstKey);
            if (keySet.isEmpty()) {
                accessedKeys.remove(usedKeysEntry.getKey());
            }
            items.remove(firstKey);
        }

        var previous = items.put(key, new ValueStat<>(value, 0));
        if (previous != null) {
            removeKey(previous.accessed(), key);
        }

        setUsedKey(0L, key);
        return previous == null ? null : previous.value();
    }

    /**
     * Get the value associated with the key, or insert the value from the supplier and return it.
     *
     * @param key   the key.
     * @param value supplier of the value if it doesn't already exist.
     * @return the value from the cache associated with the key, or the value from the supplier after it has been inserted.
     */
    @Override
    public V getOrCompute(K key, Supplier<V> value) {
        final var existingValue = get(key);

        if (existingValue != null) {
            return existingValue;
        }

        final var newValue = value.get();
        put(key, newValue);
        return newValue;
    }

    /**
     * Size of the cache (number of items).
     *
     * @return number of items in the cache.
     */
    @Override
    public int size() {
        return items.size();
    }

    private void setUsedKey(long accessed, K key) {
        final var existing = accessedKeys.get(accessed);
        if (existing != null) {
            existing.add(key);
        } else {
            final var keys = new HashSet<K>();
            keys.add(key);
            accessedKeys.put(accessed, keys);
        }
    }

    private void removeKey(long accessed, K key) {
        final var keySet = accessedKeys.get(accessed);
        keySet.remove(key);
        if (keySet.isEmpty()) {
            accessedKeys.remove(accessed);
        }
    }
}
