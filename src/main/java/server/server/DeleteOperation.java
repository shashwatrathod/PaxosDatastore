package server.server;

import server.datastore.DataStore;

import java.rmi.RemoteException;
import java.util.Objects;

/**
 * Represents the operation of deleting a key-value pair from the datastore.
 */
public class DeleteOperation extends AbstractOperation {
    public DeleteOperation(Integer key) throws RemoteException {
        super(key);
    }

    @Override
    public boolean execute(DataStore<Integer, String> dataStore) {
        if (dataStore == null) {
            throw new IllegalArgumentException("data store is null");
        }

        return dataStore.delete(this.key);
    }

    @Override
    public String toString() {
        return String.format("DELETE(%d)", key);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeleteOperation)) return false;

        DeleteOperation another = (DeleteOperation) obj;

        return this.key.equals(another.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key);
    }
}
