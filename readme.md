# JavAnton

This is (or will be) my collection/library of Java various experiments.

## ItemLimitedCache

A simple cache that will keep at most X items. Every time an item is accessed a counter for that item goes up. If the cache is full when a new items is added, one of the items with the lowest access count is discarded (if many items has the same access score, it will be the first one defined by the iterator of the underlying set).

It is implemented using a HashMap for fast access of the items, and a TreeMap for accessing the least used items.

It is not thread safe and not by any means thoroughly tested.

### Example

```java
// New cache that can hold 3 items
ItemLimitedCache<String, String> cache = new ItemLimitedCache<>(3);

cache.put("A", "A");
cache.put("B", "B");
cache.put("C", "C");

cache.get("A");
cache.get("A");
cache.get("A");
// A has been accessed 3 times

cache.get("B");
cache.get("B");
// B has been accessed 2 times

cache.get("C");
// C has been accessed 1 time

cache.put("D", "D");
// C is replaced by D
```