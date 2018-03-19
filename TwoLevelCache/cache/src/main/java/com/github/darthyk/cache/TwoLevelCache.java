package com.github.darthyk.cache;

import com.github.darthyk.cache.strategies.LeastFrequentlyUsed;
import com.github.darthyk.cache.strategies.Strategy;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;

/**
 * Class represents work with two level cache - RAM memory cache and file system memory cache
 *
 * @param <K> any key value
 * @param <V> any value
 * @author Vladislav Sazhin
 */
@Slf4j
public class TwoLevelCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private static final int REBALANCE_COUNTER = 20;
    private final MemoryCache<K, V> firstLevelCache;
    private final FileSystemCache<K, V> secondLevelCache;
    private final Strategy strategy;
    private int callingCounter = 0;

    /**
     * Initializes {@code TwoLevelCache} with default {@code LeastFrequentlyUsed} strategy
     *
     * @param memoryCacheCapacity Memory cache capacity
     * @param fileSystemCacheCapacity File system cache capacity
     */
    TwoLevelCache(int memoryCacheCapacity, int fileSystemCacheCapacity) {
        this.strategy = new LeastFrequentlyUsed();
        this.firstLevelCache = new MemoryCache<>(memoryCacheCapacity,  strategy.getInstance());
        this.secondLevelCache = new FileSystemCache<>(fileSystemCacheCapacity,  strategy.getInstance());

    }

    /**
     * Initializes {@code TwoLevelCache}
     *
     * @param memoryCacheCapacity Memory cache capacity
     * @param fileSystemCacheCapacity File system cache capacity
     * @param strategy Substitution strategy
     */
    TwoLevelCache(int memoryCacheCapacity, int fileSystemCacheCapacity, Strategy strategy) {
        this.strategy = strategy;
        this.firstLevelCache = new MemoryCache<>(memoryCacheCapacity, strategy.getInstance());
        this.secondLevelCache = new FileSystemCache<>(fileSystemCacheCapacity, strategy.getInstance());
    }

    /**
     * Caches provided object value with provided key
     *
     * @param key Key value
     * @param value Object value
     */
    @Override
    public void putToCache(K key, V value) {
        if(!firstLevelCache.containsKey(key) && !secondLevelCache.containsKey(key) && firstLevelCache.hasEmptySpace()) {
            firstLevelCache.putToCache(key, value);
            firstLevelCache.getStrategy().setFrequencyData(firstLevelCache.getFrequencyMap());
            log.debug("Put object with key %s to first level cache", key);
        } else if (!secondLevelCache.containsKey(key) && secondLevelCache.hasEmptySpace()) {
            secondLevelCache.putToCache(key, value);
            secondLevelCache.getStrategy().setFrequencyData(secondLevelCache.getFrequencyMap());
            log.debug("Put object with key %s to second level cache", key);
        } else {
            freeSpace();
            firstLevelCache.putToCache(key, value);
            firstLevelCache.getStrategy().setFrequencyData(firstLevelCache.getFrequencyMap());
            log.debug("Put object with key %s to first level cache", key);
        }

    }

    /**
     * Frees space in cache
     */
    public void freeSpace(){
        K objectToDelete = (K)secondLevelCache.getStrategy().getKeyForSubstitution();
        log.debug("Delete object with key %s according to substitution strategy %s from second level cache",
                objectToDelete, secondLevelCache.getStrategy().getClass().getSimpleName());
        secondLevelCache.deleteObject(objectToDelete);

        K objectToMove = (K)firstLevelCache.getStrategy().getKeyForSubstitution();
        Long frequencyData = firstLevelCache.getFrequencyMap().get(objectToMove);
        V objectToMoveData = firstLevelCache.removeObject(objectToMove);
        log.debug("Move object with key %s according to substitution strategy %s from first level cache to second level",
                objectToMove, firstLevelCache.getStrategy().getClass().getSimpleName());

        secondLevelCache.transferDataFromAnotherCache(objectToMove, objectToMoveData, frequencyData);
        log.debug("Delete object with key %s according to substitution strategy %s from first level cache",
                objectToMove, firstLevelCache.getStrategy().getClass().getSimpleName());
        firstLevelCache.deleteObject(objectToMove);
    }

    /**
     * Represents information about cache usage
     *
     * @return information about cache usage
     */
    public String getCacheUsage() {
        return new StringBuffer().append("First level cache usage: ").append(firstLevelCache.getFrequencyMap())
                .append("; Second level cache usage: ").append(secondLevelCache.getFrequencyMap()).append("\n").toString();
    }

    /**
     * Rebalances 1/4 of cache data between two levels (Memory and File System)
     */
    private void rebalanceDataOnTwoLevels() {
        for (int iteration = 0; iteration < size()/4; ++iteration) {
            K candidateForSlowCache = (K)firstLevelCache.getStrategy().getKeyForSubstitution();
            K candidateForFastCache = (K)secondLevelCache.getStrategy().getCandidateForMemoryCache();
            Long frequencyDataFirstLevel = firstLevelCache.getFrequencyMap().get(candidateForSlowCache);
            Long frequencyDataSecondLevel = secondLevelCache.getFrequencyMap().get(candidateForFastCache);
            V firstLevelValue = firstLevelCache.removeObject(candidateForSlowCache);
            V secondLevelValue = secondLevelCache.removeObject(candidateForFastCache);
            firstLevelCache.transferDataFromAnotherCache(candidateForFastCache, secondLevelValue, frequencyDataSecondLevel);
            secondLevelCache.transferDataFromAnotherCache(candidateForSlowCache, firstLevelValue, frequencyDataFirstLevel);
        }
    }

    /**
     * Retrieves object for provided key from cache.
     * Increments frequency usage for provided object key.
     *
     * @param key Key object
     * @return Value for provided key from cache, {@code null} if key objct is absent
     */
    @Override
    public V getObject(K key) {
        if(callingCounter == REBALANCE_COUNTER) {
            rebalanceDataOnTwoLevels();
            callingCounter = 0;
        } else ++callingCounter;
        if(firstLevelCache.containsKey(key)) {
            long frequency = firstLevelCache.getFrequencyMap().remove(key);
            firstLevelCache.getFrequencyMap().put(key, firstLevelCache.getStrategy().updateFrequency(frequency));
            return firstLevelCache.getObject(key);
        } else if (secondLevelCache.containsKey(key)) {
            long frequency = secondLevelCache.frequencyMap.remove(key);
            secondLevelCache.frequencyMap.put(key, secondLevelCache.strategyType.updateFrequency(frequency));
            return secondLevelCache.getObject(key);
        } else {
            return null;
        }

    }

    /**
     * Deletes object from cache for provided key
     *
     * @param key Key value
     */
    @Override
    public void deleteObject(K key) {
        if(firstLevelCache.containsKey(key)) {
            log.info("Delete object with key %s from first level cache", key);
            firstLevelCache.deleteObject(key);
        } else if (secondLevelCache.containsKey(key)) {
            log.info("Delete object with key %s from second level cache", key);
            secondLevelCache.deleteObject(key);
        } else {
            log.error("Object with key %s is absent in cache", key);
        }
    }

    /**
     * Removes object from cache for provided key
     *
     * @param key Key value
     * @return Object value for provided key, {@code null} if key is absent
     */
    @Override
    public V removeObject(K key) {
        if(firstLevelCache.containsKey(key)) {
            log.info("Delete object with key %s from first level cache", key);
            return firstLevelCache.removeObject(key);
        } else if (secondLevelCache.containsKey(key)) {
            log.info("Delete object with key %s from second level cache", key);
            return secondLevelCache.removeObject(key);
        } else {
            log.error("Object with key %s is absent in cache", key);
            return null;
        }
    }

    public K getKeyToBeDeleted() {
        return (K)secondLevelCache.getStrategy().getKeyForSubstitution();
    }

    /**
     * Clears cache from all values
     */
    @Override
    public void clearCache() {
        secondLevelCache.clearCache();
        firstLevelCache.clearCache();
    }

    /**
     * Checks whether cache contains provided key
     *
     * @param key Key values
     * @return {@code true} if key is present in cache, {@code false} otherwise
     */
    @Override
    public boolean containsKey(K key) {
        return firstLevelCache.containsKey(key) || secondLevelCache.containsKey(key);
    }

    /**
     * Retrieves size for memory cache
     *
     * @return Memory cache size
     */
    @Override
    public int size() {
        return firstLevelCache.size() + secondLevelCache.size();
    }

    /**
     * Checks whether cache has empty space
     *
     * @return {@code true} if cache has empty space, {@code false} otherwise
     */
    @Override
    public boolean hasEmptySpace() {
        return ((firstLevelCache.getCapacity() + secondLevelCache.capacity) > (firstLevelCache.size() + secondLevelCache.size()));
    }

    /**
     * Retrieves {@code Strategy} instance for this cache
     *
     * @return {@code Strategy} instance for this cache
     */
    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }
}
