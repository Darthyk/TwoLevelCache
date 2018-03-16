package com.github.darthyk.cache;

import com.github.darthyk.cache.strategies.Strategy;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Class represents work with file system memory cache
 *
 * @param <K> any key value
 * @param <V> any value
 * @author Vladislav Sazhin
 */
@Slf4j
public class FileSystemCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    HashMap<K, V> cacheMap;
    TreeMap<K, Long> frequencyMap;
    Path cachePath;
    File cacheFile;
    UUID cacheFileUUID = UUID.randomUUID();
    Strategy strategyType;
    int capacity;

    /**
     * Initializes cache with provided capacity
     *
     * @param capacity Cache size
     */
    FileSystemCache(int capacity, Strategy strategyType) {
        try {
            cachePath = Files.createTempDirectory("cache");
        } catch (IOException e) {
            log.error("Can't create directory");
        }
        cachePath.toFile().deleteOnExit();
        cacheFile = new File(cachePath.toFile().getAbsolutePath() + File.separatorChar
                + cacheFileUUID + ".tmp");
        cacheMap = new HashMap<>();
        frequencyMap = new TreeMap<>();
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
        writeCacheToFile(cacheMap);
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
        log.error("Object with key %s will be deleted", objectToDelete);
        deleteObject(objectToDelete);
    }

    /**
     * Writes cache to file
     */
    public synchronized void writeCacheToFile(Map<K, V> map) {
        FileOutputStream fileStream = null;
        ObjectOutputStream objectStream = null;
        try {
            fileStream = new FileOutputStream(cacheFile);
        } catch (FileNotFoundException e) {
            log.error("Can't find file %s \n Stack trace:\n %s", cacheFile.getAbsolutePath(), e.getMessage());
        }
        try {
            objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(map);
        } catch (IOException e) {
            log.error("Can't write to %s \n Stack trace:\n %s", cacheFile.getAbsolutePath(), e.getMessage());
        }
        try {
            objectStream.close();
            fileStream.flush();
            fileStream.close();
        } catch (IOException e) {
            log.error("Can't close File and Object streams.\n Stack trace:\n %s", e.getMessage());
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
        if(containsKey(key)) {
            long frequency = frequencyMap.remove(key);
            frequencyMap.put(key, strategyType.updateFrequency(frequency));
            return getDeserializedCacheMap().get(key);
        } else
            return null;
    }

    /**
     * Retrieves deserialized cache object
     *
     * @return {@code HashMap} with cache
     */
    public synchronized HashMap<K, V> getDeserializedCacheMap() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(cacheFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            log.error("Can't find file %s \n Stack trace:\n %s", cacheFile.getAbsolutePath(), e.getMessage());
        }

        ObjectInputStream objectInputStream = null;
        HashMap<K, V> deserializedObject = null;
        try {
            objectInputStream = new ObjectInputStream(fileInputStream);
            deserializedObject = (HashMap<K, V>)objectInputStream.readObject();
        } catch (IOException e) {
            log.error("Can't write to %s \n Stack trace:\n %s", cacheFile.getAbsolutePath(), e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Can't find %s class. \n Stack trace:\n %s", HashMap.class.getName(), e.getMessage());
        }

        try {
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            log.error("Can't close File and Object streams.\n Stack trace:\n %s", e.getMessage());
        }
        return deserializedObject;
    }

    /**
     * Deletes object from cache for provided key
     *
     * @param key Key value
     */
    @Override
    public synchronized void deleteObject(K key) {
        if(containsKey(key)) {
            cacheMap.remove(key);
            frequencyMap.remove(key);
            if (cacheFile.delete()) {
                new File(cachePath.toFile().getAbsolutePath() + File.pathSeparator
                        + cacheFileUUID + ".tmp");
            } else {
                log.error("Can't delete file %s", cacheFile.getAbsolutePath());
            }
            writeCacheToFile(cacheMap);
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
            V value = getDeserializedCacheMap().get(key);
            deleteObject(key);
            return value;
        } else
            return null;
    }

    /**
     * Clears cache from all values
     */
    @Override
    public synchronized void clearCache() {
        cacheMap.clear();
        frequencyMap.clear();
        if (cacheFile.delete()) {
            log.error("Cache file %s has been deleted", cacheFile.getAbsolutePath());
        } else {
            log.error("Can't delete cache file %s", cacheFile.getAbsolutePath());
        }
    }

    /**
     * Checks whether cache contains provided key
     *
     * @param key Key values
     * @return {@code true} if key is present in cache, {@code false} otherwise
     */
    @Override
    public boolean containsKey(K key) {
        return cacheMap.containsKey(key);
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
