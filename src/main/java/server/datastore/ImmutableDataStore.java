package server.datastore;

public interface ImmutableDataStore<K,V> {
    /**
     * If the given key exists in our datastore, returns the value for the key.
     *
     * @param key the key for which the value is to be queried
     * @return the value if key exists, else null.
     * @throws IllegalArgumentException if key is null.
     */
    V get(K key);
}
