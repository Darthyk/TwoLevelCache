package com.github.darthyk.cache;

import com.github.darthyk.cache.strategies.Strategy;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;

/**
 * Class represents work with RAM memory cache
 *
 * @param <K> any key value
 * @param <V> any value
 * @author Vladislav Sazhin
 */
@Slf4j
public class MemoryCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private HashMap<K, V> cacheMap;
    private TreeMap<K, Long> frequencyMap;
    private Strategy strategyType;
    private int capacity;

    /**
     * Initializes cache with provided capacity
     *
     * @param capacity Cache capacity
     */
    MemoryCache(int capacity, Strategy strategyType) {
        this.cacheMap = new HashMap<>();
        this.frequencyMap = new TreeMap<>();
        this.strategyType = strategyType;
        this.capacity = capacity;
    }

    /**
     * Caches provided object value with provided key
     *
     * @param key Key value
     * @param value Object value
     */
    @Override
    public void putToCache(K key, V value) {
        if(!hasEmptySpace()) {
            freeSpace();
        }
        cacheMap.put(key, value);
        frequencyMap.put(key, strategyType.fillFrequency());
        strategyType.setFrequencyData(frequencyMap);
    }

    /**
     * Transfers data from another cache
     *
     * @param key Key value
     * @param value Object value
     * @param frequencyData {@code Long} value from previous cache
     */
    public void transferDataFromAnotherCache(K key, V value, Long frequencyData) {
        cacheMap.put(key, value);
        frequencyMap.put(key, frequencyData);
        strategyType.setFrequencyData(frequencyMap);
    }

    /**
     * Frees space in cache according to substitution strategy
     */
    @Override
    public void freeSpace() {
        K objectToDelete = (K)this.strategyType.getKeyForSubstitution();
        log.info("Object with key % will be deleted from cache according to substitution strategy %s", objectToDelete,
                strategyType.getClass().getSimpleName());
        deleteObject(objectToDelete);
    }

    /**
     * Retrieves object for provided key from cache.
     * Increments frequency usage for provided object key.
     *
     * @param key Key object
     * @return Value for provided key from cache, {@code null} if key is absent
     */
    @Override
    public V getObject(K key) {
        if(containsKey(key)) {
            long frequency = frequencyMap.remove(key);
            frequencyMap.put(key, strategyType.updateFrequency(frequency));
            return cacheMap.get(key);
        } else
            return null;
    }

    /**
     * Deletes object from cache for provided key
     *
     * @param key Key value
     */
    @Override
    public void deleteObject(K key) {
        if(containsKey(key)) {
            cacheMap.remove(key);
            frequencyMap.remove(key);
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
        if(containsKey(key)) {
            frequencyMap.remove(key);
            return cacheMap.remove(key);
        } else
            return null;
    }

    /**
     * Clears cache from all values
     */
    @Override
    public void clearCache() {
        cacheMap.clear();
        frequencyMap.clear();
    }

    /**
     * Checks whether cache contains provided key
     *
     * @param key Key values
     * @return {@code true} if key is present in cache, {@code false} otherwise
     */
    @Override
    public boolean containsKey(K key) {
        return this.cacheMap.containsKey(key);
    }

    /**
     * Retrieves size for memory cache
     *
     * @return Memory cache size
     */
    @Override
    public int size() {
        return this.cacheMap.size();
    }

    /**
     * Checks whether cache has empty space
     *
     * @return {@code true} if cache has empty space, {@code false} otherwise
     */
    @Override
    public boolean hasEmptySpace() {
        return size() < this.capacity;
    }

    /**
     * Retrieves {@code Strategy} instance for this cache
     *
     * @return {@code Strategy} instance for this cache
     */
    @Override
    public Strategy getStrategy() {
        return this.strategyType;
    }

    /**
     * Retrieves frequency {@code TreeMap} for this cache
     *
     * @return frequency {@code TreeMap} for this cache
     */
    public TreeMap<K, Long> getFrequencyMap() {
        return this.frequencyMap;
    }

    /**
     * Retrieves capacity of this cache
     *
     * @return capacity of this cache
     */
    public int getCapacity() {
        return this.capacity;
    }
}
