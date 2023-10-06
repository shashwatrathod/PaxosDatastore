package server.datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteDataStore<K,V> extends Remote {

    /**
     * Sets the value of a particular key in our datastore. If the key already exists,
     * updates the replaces the old value for the key with current value.
     *
     * @param key   key for the data
     * @param value the data to be stored
     * @throws IllegalArgumentException if key or value are null
     */
    boolean put(K key, V value) throws RemoteException;

    /**
     * If the given key exists in our datastore, returns the value for the key.
     *
     * @param key the key for which the value is to be queried
     * @return the value if key exists, else null.
     * @throws IllegalArgumentException if key is null.
     */
    V get(K key) throws RemoteException;

    /**
     * If the key exists in our datastore, deletes the key-value pair.
     *
     * @param key the key to be deleted
     * @return whether the data was deleted. False if the key was not found.
     * @throws IllegalArgumentException if the key is null.
     */
    boolean delete(K key) throws RemoteException;
}
