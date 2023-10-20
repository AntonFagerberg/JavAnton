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

    public ItemLimitedCache(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        items = new HashMap<>(size);
        accessedKeys = new TreeMap<>();
        maxSize = size;
    }

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

    @Override
    public V remove(K key) {
        final var valueStat = items.remove(key);
        if (valueStat == null) {
            return null;
        }

        removeKey(valueStat.accessed(), key);
        return valueStat.value();
    }

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

    @Override
    public V getOrCompute(K key, Supplier<V> v) {
        final var value = get(key);

        if (value != null) {
            return value;
        }

        final var newValue = v.get();
        put(key, newValue);
        return newValue;
    }

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
