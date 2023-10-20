package com.antonfagerberg.javanton.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class ItemLimitedCacheTest {

    Cache<String, String> cache;

    @BeforeEach
    void setup() {
        cache = new ItemLimitedCache<>(3);
    }

    @Test
    void sizeShouldBeZeroAtStart() {
        assertEquals(0, cache.size());
    }

    @Test
    void sizeShouldBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new ItemLimitedCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new ItemLimitedCache<>(-1));
    }

    @Test
    void incrementSize() {
        assertEquals(0, cache.size());
        cache.put("A", "A");
        assertEquals(1, cache.size());
        cache.put("B", "B");
        assertEquals(2, cache.size());
    }

    @Test
    void maxSize() {
        assertEquals(0, cache.size());
        cache.put("A", "A");
        assertEquals(1, cache.size());
        cache.put("B", "B");
        assertEquals(2, cache.size());
        cache.put("C", "C");
        assertEquals(3, cache.size());
        cache.put("D", "D");
        assertEquals(3, cache.size());
    }

    @Test
    void removeLeastUsed() {
        assertEquals(0, cache.size());
        assertNull(cache.put("A", "A"));
        assertEquals(1, cache.size());
        assertNull(cache.put("B", "B"));
        assertEquals(2, cache.size());
        assertNull(cache.put("C", "C"));
        assertEquals(3, cache.size());

        assertEquals("A", cache.get("A"));
        assertEquals("A", cache.get("A"));
        assertEquals("A", cache.get("A"));

        assertEquals("B", cache.get("B"));
        assertEquals("B", cache.get("B"));

        assertEquals("C", cache.get("C"));

        assertNull(cache.put("D", "D"));
        assertEquals(3, cache.size());
        assertNull(cache.get("C"));

        assertNull(cache.put("E", "E"));
        assertEquals(3, cache.size());
        assertNull(cache.get("D"));

        assertEquals("E", cache.get("E"));
        assertEquals("E", cache.get("E"));
        assertEquals("E", cache.get("E"));

        assertNull(cache.put("F", "F"));
        assertEquals(3, cache.size());
        assertNull(cache.get("B"));
    }

    @Test
    void remove() {
        assertNull(cache.put("A", "A"));
        assertNull(cache.put("B", "B"));
        assertNull(cache.put("C", "C"));
        assertEquals(3, cache.size());

        assertEquals("A", cache.get("A"));
        assertEquals("B", cache.get("B"));
        assertEquals("C", cache.get("C"));

        assertEquals("A", cache.remove("A"));
        assertEquals(2, cache.size());
        assertEquals("B", cache.remove("B"));
        assertEquals(1, cache.size());
        assertEquals("C", cache.remove("C"));
        assertEquals(0, cache.size());

        assertNull(cache.get("A"));
        assertNull(cache.get("B"));
        assertNull(cache.get("C"));
        assertEquals(0, cache.size());
    }

    @Test
    void replace() {
        assertNull(cache.put("A", "A"));
        assertNull(cache.put("B", "B"));
        assertNull(cache.put("C", "C"));
        assertEquals(3, cache.size());

        assertEquals("A", cache.get("A"));
        assertEquals("A", cache.get("A"));
        assertEquals("A", cache.get("A"));

        assertEquals("B", cache.get("B"));
        assertEquals("B", cache.get("B"));

        assertEquals("C", cache.get("C"));
        assertEquals("C", cache.get("C"));

        assertEquals("A", cache.put("A", "AA"));

        assertNull(cache.put("D", "D"));
        assertNull(cache.get("A"));
    }

    @Test
    void getOrCompute() {
        final int[] i = {0};
        Supplier<String> computeA = () -> {
            i[0]++;
            return "A";
        };

        assertEquals(0, i[0]);
        assertEquals("A", cache.getOrCompute("A", computeA));
        assertEquals(1, i[0]);
        assertEquals("A", cache.getOrCompute("A", computeA));
        assertEquals(1, i[0]);
    }
}
