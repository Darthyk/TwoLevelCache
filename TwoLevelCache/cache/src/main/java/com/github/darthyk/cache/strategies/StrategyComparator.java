package com.github.darthyk.cache.strategies;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public class StrategyComparator<K> implements Comparator<K>, Serializable {

    private final Map<K, Long> comparatorMap;

    StrategyComparator(Map<K, Long> comparatorMap) {
        this.comparatorMap = comparatorMap;
    }

    @Override
    public int compare(K key1, K key2) {
        long key1Value = comparatorMap.get(key1);
        long key2value = comparatorMap.get(key2);
        if (key1Value > key2value) {
            return 1;
        } else if (key1Value < key2value) {
            return -1;
        } else {
            return 0;
        }
    }
}
