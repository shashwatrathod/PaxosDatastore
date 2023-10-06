package server.datastore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentDataStoreImpl<K, V> implements DataStore<K, V> {

    protected final Map<K, V> store;

    public ConcurrentDataStoreImpl() {
        this.store = new ConcurrentHashMap<>();
    }

    @Override
    public boolean put(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value is null!");
        }

        this.store.put(key, value);
        return true;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("The key is null.");
        }

        return this.store.getOrDefault(key, null);
    }

    @Override
    public boolean delete(K key) {
        if (key == null) {
            throw new IllegalArgumentException("The key is null.");
        }

        if (!this.store.containsKey(key)) {
            return false;
        }

        this.store.remove(key);
        return true;
    }
}
