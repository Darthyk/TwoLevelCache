package com.github.darthyk.cache.strategies;

import java.util.Map;
import java.util.TreeMap;

public class LeastRecentlyUsed<K> implements Strategy<K> {

    Map<K, Long> frequencyData;

    public LeastRecentlyUsed() {}

    @Override
    public LeastRecentlyUsed getInstance() {
        return new LeastRecentlyUsed();
    }

    @Override
    public void setFrequencyData(Map<K, Long> frequencyData) {
        this.frequencyData = frequencyData;
    }

    @Override
    public Map<K, Long> getFrequencyData() {
        return this.frequencyData;
    }

    @Override
    public K getCandidateForMemoryCache() {
        TreeMap<K, Long> sortedValues = new TreeMap<>(new StrategyComparator(frequencyData));
        return sortedValues.lastEntry().getKey();
    }

    @Override
    public long fillFrequency() {
        return System.nanoTime();
    }

    @Override
    public long updateFrequency(long oldValue) {
        return System.nanoTime();
    }

    @Override
    public K getKeyForSubstitution() {
        TreeMap<K, Long> sortedValues = new TreeMap<>(new StrategyComparator(frequencyData));
        return sortedValues.firstEntry().getKey();
    }
}