package com.antonfagerberg.javanton.cache;

import java.util.function.Supplier;

sealed public interface Cache<K, V> permits ItemLimitedCache {

    V get(K key);

    V remove(K key);

    V put(K key, V value);

    V getOrCompute(K key, Supplier<V> v);

    int size();

}
