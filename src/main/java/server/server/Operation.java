package server.server;

import server.datastore.DataStore;

import java.io.Serializable;

/**
 * Represents an operation that can be performed on a datastore.
 */
public interface Operation extends Serializable {

    // Extends Serializable because it needs to be sent over via a remote object.

    /**
     * Execute the operation on the datastore.
     * @param dataStore that datastore on which the operation is to be performed.
     * @return whether the operation was successful.
     */
    boolean execute(DataStore<Integer, String> dataStore);

    /**
     * Get the datastore key on which the operation is to be performed.
     */
    Integer key();
}