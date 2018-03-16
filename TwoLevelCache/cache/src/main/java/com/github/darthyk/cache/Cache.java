package com.github.darthyk.cache;

import com.github.darthyk.cache.strategies.Strategy;

public interface Cache<K, V> {
    void putToCache(K key, V value);
    V getObject(K key);
    void deleteObject(K key);
    V removeObject(K key);
    void clearCache();
    boolean containsKey(K key);
    int size();
    boolean hasEmptySpace();
    Strategy getStrategy();
    void freeSpace();
}
