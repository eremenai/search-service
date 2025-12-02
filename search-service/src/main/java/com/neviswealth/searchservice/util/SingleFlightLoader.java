package com.neviswealth.searchservice.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SingleFlightLoader<K, V> {

    private final ConcurrentHashMap<K, CompletableFuture<V>> inFlight = new ConcurrentHashMap<>();

    public V load(K key, Callable<V> loader) {
        // optimistic fast-path: maybe already loading
        CompletableFuture<V> existing = inFlight.get(key);
        if (existing != null) {
            return existing.join();
        }

        CompletableFuture<V> newFuture = new CompletableFuture<>();
        // try to register our future
        existing = inFlight.putIfAbsent(key, newFuture);
        if (existing == null) {
            // we "won": actually run the loader
            try {
                V value = loader.call();
                newFuture.complete(value);
                return value;
            } catch (Throwable t) {
                newFuture.completeExceptionally(t);
                if (t instanceof Exception e) throw new RuntimeException(e);
                throw (Error) t;
            } finally {
                // allow a new load for this key in the future
                inFlight.remove(key, newFuture);
            }
        } else {
            // someone else already started loading
            return existing.join();
        }
    }
}
