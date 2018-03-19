package com.github.darthyk.cache.strategies;

import java.util.Map;
import java.util.TreeMap;

public class LeastRecentlyUsed<K> implements Strategy<K> {

    Map<K, Long> strategyData;

    public LeastRecentlyUsed() {}

    @Override
    public LeastRecentlyUsed getInstance() {
        return new LeastRecentlyUsed();
    }

    @Override
    public void setStrategyData(Map<K, Long> strategyData) {
        this.strategyData = strategyData;
    }

    @Override
    public Map<K, Long> getStrategyData() {
        return this.strategyData;
    }

    @Override
    public K getCandidateForMemoryCache() {
        TreeMap<K, Long> sortedValues = new TreeMap<>(new StrategyComparator(strategyData));
        sortedValues.putAll(strategyData);
        return sortedValues.lastEntry().getKey();
    }

    @Override
    public long fillStrategyData() {
        return System.nanoTime();
    }

    @Override
    public long updateStrategyData(long oldValue) {
        return System.nanoTime();
    }

    @Override
    public K getKeyForSubstitution() {
        TreeMap<K, Long> sortedValues = new TreeMap<>(new StrategyComparator(strategyData));
        sortedValues.putAll(strategyData);
        return sortedValues.firstEntry().getKey();
    }
}
