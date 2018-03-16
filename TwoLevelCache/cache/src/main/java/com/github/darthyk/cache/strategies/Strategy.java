package com.github.darthyk.cache.strategies;

import java.util.Map;

public interface Strategy<K> {

    Strategy getInstance();

    long fillFrequency();

    K getKeyForSubstitution();

    long updateFrequency(long oldValue);

    void setFrequencyData(Map<K, Long> frequencyData);

    Map<K, Long> getFrequencyData();

    K getCandidateForMemoryCache();
}
