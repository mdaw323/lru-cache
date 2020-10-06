package pl.mdaw.cache

import spock.lang.Specification

class LRUCacheTest extends Specification {

    def "when cache is full check if last recently used element is removed"() {
        given:
        LRUCache cache = new LRUCache("cache_name", 2)

        when:
        cache.put(1, 1)
        cache.put(2, 2)
        cache.put(3, 3)

        then:
        cache.get(1, Integer) == null
        cache.get(2, Integer) == 2
        cache.get(3, Integer) == 3
    }

    def "check if getting element moves it into the back of LRU list (it won't be removed first)"() {
        given:
        LRUCache cache = new LRUCache("cache_name", 2)

        when:
        cache.put(1, 1)
        cache.put(2, 2)
        cache.get(1)
        cache.put(3, 3)

        then:
        cache.get(1, Integer) == 1
        cache.get(2) == null
        cache.get(3, Integer) == 3
    }

    def "when ttl expires check if cached object is removed"() {
        given:
        LRUCache cache = new LRUCache("cache_name", 3, 10L)

        when:
        cache.put(1, 1)
        cache.put(2, 2)
        cache.put(3, 3)

        then:
        cache.get(1, Integer) == 1
        cache.get(2, Integer) == 2
        cache.get(3, Integer) == 3

        sleep(30L)
        cache.get(1, Integer) == null
        cache.get(2, Integer) == null
        cache.get(3, Integer) == null
    }

    def "when cache is invalidated cached objects should be removed"() {
        given:
        LRUCache cache = new LRUCache("cache_name", 10, 10)

        when:
        cache.put(1, 1)
        cache.invalidate()

        then:
        cache.get(1, Integer) == null
    }

    def "when evict is invoked, cached object should be removed"() {
        given:
        LRUCache cache = new LRUCache("cache_name", 10, 10)

        when:
        cache.put(1, 1)
        cache.put(2, 2)
        cache.evict(1)
        cache.evictIfPresent(2)
        cache.evictIfPresent(3)

        then:
        cache.get(1, Integer) == null
        cache.get(2, Integer) == null
    }
}
