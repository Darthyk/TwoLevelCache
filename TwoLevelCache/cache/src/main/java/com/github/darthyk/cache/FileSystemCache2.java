package com.github.darthyk.cache;

import com.github.darthyk.cache.strategies.Strategy;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Class represents work with file system memory cache.
 * <p>
 * NOTE: each value will be stored in separate file.
 *
 * @param <K> any key value
 * @param <V> any value
 * @author Vladislav Sazhin
 */
@Slf4j
public class FileSystemCache2<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    HashMap<K, String> cacheMap;
    TreeMap<K, Long> strategyMap;
    Path cachePath;
    Strategy strategyType;
    int capacity;

    /**
     * Initializes cache with provided capacity
     *
     * @param capacity Cache size
     */
    FileSystemCache2(int capacity, Strategy strategyType) {
        try {
            cachePath = Files.createTempDirectory("cache");
        } catch (IOException e) {
            log.error("Can't create directory");
        }
        cachePath.toFile().deleteOnExit();
        cacheMap = new HashMap<>();
        strategyMap = new TreeMap<>();
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

        strategyMap.put(key, strategyType.fillStrategyData());
        strategyType.setStrategyData(strategyMap);
        cacheMap.put(key, writeCacheToFile(value));
    }

    /**
     * Transfers data from another cache
     *
     * @param key Key value
     * @param value Object value
     * @param frequencyData {@code Long} value from previous cache
     */
    public void transferDataFromAnotherCache(K key, V value, Long frequencyData) {
        cacheMap.put(key, writeCacheToFile(value));
        strategyMap.put(key, frequencyData);
        strategyType.setStrategyData(strategyMap);
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
    public synchronized String writeCacheToFile(V value) {
        FileOutputStream fileStream = null;
        ObjectOutputStream objectStream = null;
        File cacheFile = new File(cachePath.toFile().getAbsolutePath() + File.separatorChar
                + UUID.randomUUID() + ".tmp");
        try {
            fileStream = new FileOutputStream(cacheFile);
        } catch (FileNotFoundException e) {
            log.error("Can't find file %s \n Stack trace:\n %s", cacheFile.getAbsolutePath(), e.getMessage());
        }
        try {
            objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(value);
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
        return cacheFile.getAbsolutePath();
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
            long frequency = strategyMap.remove(key);
            strategyMap.put(key, strategyType.updateStrategyData(frequency));
            return getDeserializedObject(cacheMap.get(key));
        } else
            return null;
    }

    /**
     * Retrieves deserialized cache object
     *
     * @return deserialized cache object
     */
    public synchronized V getDeserializedObject(String cacheFilePath) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(cacheFilePath);
        } catch (FileNotFoundException e) {
            log.error("Can't find file %s \n Stack trace:\n %s", cacheFilePath, e.getMessage());
        }

        ObjectInputStream objectInputStream = null;
        V deserializedObject = null;
        try {
            objectInputStream = new ObjectInputStream(fileInputStream);
            deserializedObject = (V)objectInputStream.readObject();
        } catch (IOException e) {
            log.error("Can't write to %s \n Stack trace:\n %s", cacheFilePath, e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Can't find class. \n Stack trace:\n %s", e.getMessage());
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
     * Deletes cache object for provided key
     *
     * @param key Key value
     */
    @Override
    public synchronized void deleteObject(K key) {
        if(containsKey(key)) {
            String fileToDelete = cacheMap.remove(key);
            strategyMap.remove(key);
            if (!new File(fileToDelete).delete()) {
                log.error("Can't delete file %s", fileToDelete);
            }
        }
    }

    /**
     * Removes cache object for provided key
     *
     * @param key Key value
     * @return Object value for provided key, {@code null} if key is absent
     */
    @Override
    public V removeObject(K key) {
        if(containsKey(key)) {
            strategyMap.remove(key);
            V value = getDeserializedObject(cacheMap.get(key));
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
        for (String path : cacheMap.values()) {
            if (!new File(path).delete()) {
                log.error("Can't delete file %s", path);
            }
        }

        cacheMap.clear();
        strategyMap.clear();
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
     * Retrieves strategy {@code TreeMap} for this cache
     *
     * @return strategy {@code TreeMap} for this cache
     */
    public TreeMap<K, Long> getStrategyMap() {
        return this.strategyMap;
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
