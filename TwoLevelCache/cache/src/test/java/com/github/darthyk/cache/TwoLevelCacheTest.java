package com.github.darthyk.cache;

import com.github.darthyk.cache.strategies.LeastFrequentlyUsed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static junit.framework.TestCase.*;

public class TwoLevelCacheTest extends TestData {

    TwoLevelCache cache;

    /**
     * Precondition:
     * 1) Cache with default capacity (3) is created and initialized with 2 objects
     * 2) Least Frequently Used strategy is set by default
     */
    @Before
    public void init() {
        cache = new TwoLevelCache(2, 2, new LeastFrequentlyUsed());
        assertNotNull("Can't create cache object", cache);

        cache.putToCache(IntegerData.FIRST.getKey(), IntegerData.FIRST.getValue());
        cache.putToCache(StringData.SECOND.getKey(), StringData.SECOND.getValue());
        cache.putToCache(IntegerData.THIRD.getKey(), IntegerData.THIRD.getValue());
        assertEquals("Only 3 objects should be added while initializing cache",3, cache.size());
    }

    @After
    public void terminate() {
        cache.clearCache();
    }

    @Test
    public void checkAdditionToCache() {
        cache.putToCache(IntegerData.FOURTH.getKey(), IntegerData.FOURTH.getValue());
        assertEquals("Only 4 objects should be in cache",4, cache.size());
    }

    @Test
    public void checkObjectSubstitution() {
        cache.putToCache(StringData.FOURTH.getKey(), StringData.FOURTH.getValue());
        cache.putToCache(StringData.FIFTH.getKey(), StringData.FIFTH.getValue());
        assertEquals("Only 4 objects should be in cache",4, cache.size());
    }

    @Test
    public void checkRebalance() {
        cache = new TwoLevelCache(5, 5, new LeastFrequentlyUsed());
        cache.putToCache(1, 6256);
        cache.putToCache(2, 6546);
        cache.putToCache(3, 63656);
        cache.putToCache(4, 6376);
        cache.putToCache(5, 6056);
        cache.putToCache(6, 547);
        cache.putToCache(7, 5683);
        cache.putToCache(8, 3435);
        cache.putToCache(9, 5683);
        cache.putToCache(10, 345375);
        for (int i = 0; i < 10000; i++) {
            cache.getObject((int)((new Random().nextInt(10)) + 1));
            System.out.println(cache.getCacheUsage());
        }
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
        cache.putToCache(IntegerData.FOURTH.getKey(), IntegerData.FOURTH.getValue());
        assertFalse("Cache has empty space", cache.hasEmptySpace());
    }
}
