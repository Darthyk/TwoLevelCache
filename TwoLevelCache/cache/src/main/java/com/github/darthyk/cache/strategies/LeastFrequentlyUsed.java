package com.github.darthyk.cache.strategies;

import java.util.Map;
import java.util.TreeMap;

public class LeastFrequentlyUsed<K> implements Strategy<K> {

    Map<K, Long> frequencyData;

    @Override
    public LeastFrequentlyUsed getInstance() {
        return new LeastFrequentlyUsed();
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
        sortedValues.putAll(frequencyData);
        return sortedValues.lastEntry().getKey();
    }

    @Override
    public long fillFrequency() {
        return 1L;
    }

    @Override
    public long updateFrequency(long oldValue) {
        return oldValue + 1;
    }

    @Override
    public K getKeyForSubstitution() {
        TreeMap<K, Long> sortedValues = new TreeMap<>(new StrategyComparator(frequencyData));
        sortedValues.putAll(frequencyData);
        return sortedValues.firstEntry().getKey();
    }
}
