package server.datastore;


/**
 * Represents a data store that is used to store key-value pairs.
 * Allows operations such as adding new data, deleting data, and viewing data.
 *
 * @param <K> data-type of the key
 * @param <V> data-type of the value
 */
public interface DataStore<K, V> extends ImmutableDataStore<K, V> {

    /**
     * Sets the value of a particular key in our datastore. If the key already exists,
     * updates the replaces the old value for the key with current value.
     *
     * @param key   key for the data
     * @param value the data to be stored
     * @throws IllegalArgumentException if key or value are null
     */
    boolean put(K key, V value);

    /**
     * If the key exists in our datastore, deletes the key-value pair.
     *
     * @param key the key to be deleted
     * @return whether the data was deleted. False if the key was not found.
     * @throws IllegalArgumentException if the key is null.
     */
    boolean delete(K key);
}
