package com.github.darthyk.cache;

import com.github.darthyk.cache.strategies.LeastFrequentlyUsed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.*;

public class FileSystemTest extends TestData {

    FileSystemCache cache;

    /**
     * Precondition:
     * 1) Cache with default capacity (3) is created and initialized with 2 objects
     * 2) Least Frequently Used strategy is set by default
     */
    @Before
    public void init() {
        cache = new FileSystemCache(DEFAULT_CAPACITY, new LeastFrequentlyUsed());
        assertNotNull("Can't create cache object", cache);

        cache.putToCache(IntegerData.FIRST.getKey(), IntegerData.FIRST.getValue());
        cache.putToCache(StringData.SECOND.getKey(), StringData.SECOND.getValue());
        assertEquals("Only 2 objects should be added while initializing cache",2, cache.size());
    }

    @After
    public void terminate() {
        cache.clearCache();
    }

    @Test
    public void checkAdditionToCache() {
        cache.putToCache(IntegerData.THIRD.getKey(), IntegerData.THIRD.getValue());
        assertEquals("Only 3 objects should be in cache",3, cache.size());
    }

    @Test
    public void checkObjectSubstitution() {
        cache.putToCache(IntegerData.THIRD.getKey(), IntegerData.THIRD.getValue());
        cache.putToCache(IntegerData.FOURTH.getKey(), IntegerData.FOURTH.getValue());
        assertEquals("Only 3 objects should be in cache",3, cache.size());
    }

    @Test
    public void checkExistentObjectRetrieval() {
        Integer retrievedObject = (Integer)cache.getObject(IntegerData.FIRST.getKey());
        assertNotNull("Retrieved object mustn't be null", retrievedObject);
        assertEquals("Retrieved object is not equal to expected", IntegerData.FIRST.getValue(), retrievedObject);
    }

    @Test
    public void checkNonExistentObjectRetrieval() {
        assertFalse("Cache contains non existent object key", cache.containsKey(IntegerData.FIFTH.getKey()));
        Integer retrievedObject = (Integer)cache.getObject(IntegerData.FIFTH.getKey());
        assertNull("Retrieved object must be null", retrievedObject);
    }

    @Test
    public void checkObjectDeletion() {
        cache.deleteObject(IntegerData.FIRST.getKey());
        Integer retrievedObject = (Integer)cache.getObject(IntegerData.FIRST.getKey());
        assertNull("Retrieved deleted object must be null", retrievedObject);
    }

    @Test
    public void checkObjectRemoval() {
        Integer removedObject = (Integer)cache.removeObject(IntegerData.FIRST.getKey());
        assertNotNull("Retrieved object mustn't be null", removedObject);
        assertEquals("Removed object is not equal to expected", IntegerData.FIRST.getValue(), removedObject);

        Integer retrievedObject = (Integer)cache.getObject(IntegerData.FIRST.getKey());
        assertNull("Retrieved deleted object must be null", retrievedObject);
    }

    @Test
    public void checkCacheClearing() {
        cache.clearCache();
        assertEquals("Cache is not cleared", cache.size(), 0);
    }

    @Test
    public void checkCacheEmptySpace() {
        assertTrue("Cache has not empty space", cache.hasEmptySpace());
        cache.putToCache(IntegerData.THIRD.getKey(), IntegerData.THIRD.getValue());
        assertFalse("Cache has empty space", cache.hasEmptySpace());
    }
}
