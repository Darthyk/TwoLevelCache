package com.github.darthyk.cache.strategies;

import java.util.Map;

public interface Strategy<K> {

    Strategy getInstance();

    long fillStrategyData();

    K getKeyForSubstitution();

    long updateStrategyData(long oldValue);

    void setStrategyData(Map<K, Long> frequencyData);

    Map<K, Long> getStrategyData();

    K getCandidateForMemoryCache();
}
