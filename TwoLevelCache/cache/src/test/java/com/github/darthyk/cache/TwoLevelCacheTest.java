package com.github.darthyk.cache;

import com.github.darthyk.cache.strategies.LeastFrequentlyUsed;
import com.github.darthyk.cache.strategies.LeastRecentlyUsed;
import com.github.darthyk.cache.strategies.MostRecentlyUsed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void checkMostRecentlyUsedStrategy() {
        cache = new TwoLevelCache(4, 4, new MostRecentlyUsed());
        cache.putToCache(1, 6256);
        cache.putToCache(2, 6546);
        cache.putToCache(3, 63656);
        cache.putToCache(4, 6376);
        cache.putToCache(5, 6056);
        cache.putToCache(6, 547);
        cache.putToCache(7, 5683);
        cache.putToCache(8, 3435);
        for (int i = 0; i < 10; i++) {
            cache.getObject(1);
        }
        for (int i = 0; i < 1000; i++) {
            cache.getObject(2);
        }
        final int lastUsedObjectKey = 6;
        for (int i = 0; i < 100; i++) {
            cache.getObject(lastUsedObjectKey);
        }
        int keyToBeDeleted = (int)cache.getKeyToBeDeleted();
        assertEquals("Expected key for deletion should be " + lastUsedObjectKey, lastUsedObjectKey, keyToBeDeleted);
        cache.putToCache(11, 77777777);
        assertFalse("Object with key " + keyToBeDeleted + " wasn't deleted after substitution",
                cache.containsKey(keyToBeDeleted));
    }

    @Test
    public void checkLeastFrequentlyUsedStrategy() {
        cache = new TwoLevelCache(4, 4, new LeastFrequentlyUsed());
        cache.putToCache(1, 6256);
        cache.putToCache(2, 6546);
        cache.putToCache(3, 63656);
        cache.putToCache(4, 6376);
        cache.putToCache(5, 6056);
        cache.putToCache(6, 547);
        cache.putToCache(7, 5683);
        cache.putToCache(8, 3435);
        final int notUsedObjectKey = 8;
        for (int i = 0; i < 10; i++) {
            for (int j = 1; j < notUsedObjectKey; j++) {
                cache.getObject(j);
            }
        }
        int keyToBeDeleted = (int)cache.getKeyToBeDeleted();
        assertEquals("Expected key for deletion should be " + notUsedObjectKey, notUsedObjectKey, keyToBeDeleted);
        cache.putToCache(11, 77777777);
        assertFalse("Object with key " + keyToBeDeleted + " wasn't deleted after substitution",
                cache.containsKey(keyToBeDeleted));
    }

    @Test
    public void checkLeastRecentlyUsedStrategy() {
        cache = new TwoLevelCache(4, 4, new LeastRecentlyUsed());
        cache.putToCache(1, 6256);
        cache.putToCache(2, 6546);
        cache.putToCache(3, 63656);
        cache.putToCache(4, 6376);
        cache.putToCache(5, 6056);
        cache.putToCache(6, 547);
        cache.putToCache(7, 5683);
        cache.putToCache(8, 3435);
        final int firstUsedObjectKey = 1;
        cache.getObject(firstUsedObjectKey);
        for (int i = 0; i < 10; i++) {
            for (int j = 2; j <= 8; j++) {
                cache.getObject(j);
            }
        }
        int keyToBeDeleted = (int)cache.getKeyToBeDeleted();
        assertEquals("Expected key for deletion should be " + firstUsedObjectKey, firstUsedObjectKey, keyToBeDeleted);
        cache.putToCache(11, 77777777);
        assertFalse("Object with key " + keyToBeDeleted + " wasn't deleted after substitution",
                cache.containsKey(keyToBeDeleted));
    }

    @Test
    public void checkStrategy() {

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
