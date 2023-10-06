package server.datastore;


import java.util.HashSet;
import java.util.Set;

/**
 * Represents an implementation of {@link LockableDataStore} where PUT and DELETE operations fail on keys that are locked.
 */
public class LockableDataStoreImpl<K, V> extends ConcurrentDataStoreImpl<K, V> implements LockableDataStore<K, V> {

    private final Set<K> locked;

    public LockableDataStoreImpl() {
        super();
        this.locked = new HashSet<>();
    }

    @Override
    public boolean lock(K key) {
        if (key == null) {
            throw new IllegalArgumentException("The key is null.");
        }

        // We should be able to lock a key even if the key doesn't exist in our store; this key may be added in the future.

        // if the key has already been locked, that means we shouldn't allow another thread to acq lock on the same resource.
        if (this.locked.contains(key)) return false;

        this.locked.add(key);

        return true;
    }

    @Override
    public boolean unlock(K key) {
        if (key == null) {
            throw new IllegalArgumentException("The key is null.");
        }

        if (!this.locked.contains(key)) return false;

        this.locked.remove(key);

        return true;
    }

    @Override
    public boolean put(K key, V value) {

        // If our resource is locked, we cannot modify it.
        if (this.locked.contains(key)) return false;

        return super.put(key, value);
    }

    @Override
    public boolean delete(K key) {

        // If our resource is locked, we cannot modify it.
        if (this.locked.contains(key)) return false;

        return super.delete(key);
    }
}
