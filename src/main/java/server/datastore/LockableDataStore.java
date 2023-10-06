package server.datastore;

/**
 * Represents a datastore where certain key-value pairs can be locked to
 * prevent any modifications. Values can still be read if locked.
 */
public interface LockableDataStore<K, V> extends DataStore<K, V> {

    /**
     * Puts a lock on certain key-value pair. Once a lock is in place,
     * the pair cannot be modified/deleted unless unlocked. A value can still be read
     * even if the pair is locked.
     * If a lock is put on a key that doesn't exist in the datastore, then while the lock
     * is in place, a value with that key cannot be added to the store.
     * @param key the key to be locked.
     * @return false if the key is already locked else true.
     */
    boolean lock(K key);

    /**
     * Unlocks a key. Once a key is unlocked, it can be modified/deleted.
     * @param key the key to be unlocked
     * @return false if key is not locked, else true.
     */
    boolean unlock(K key);
}
