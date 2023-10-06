package server.server;

import server.datastore.DataStore;

import java.rmi.RemoteException;
import java.util.Objects;

/**
 * Represents the operation of creating a new entry or updating an existing entry
 * using the key, value specified in the datastore.
 */
public class PutOperation extends AbstractOperation {

    private final String value;

    public PutOperation(Integer key, String value) throws RemoteException {
        super(key);

        if (value == null) {
            throw new IllegalArgumentException("The value is null.");
        }

        this.value = value;
    }

    @Override
    public boolean execute(DataStore<Integer, String> dataStore) {
        if (dataStore == null) {
            throw new IllegalArgumentException("The datastore is null.");
        }

        return dataStore.put(this.key, this.value);
    }

    @Override
    public String toString() {
        return String.format("PUT(%d, %s)", key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PutOperation)) return false;

        PutOperation another = (PutOperation) obj;

        return (Objects.equals(this.key, another.key)) && this.value.equals(another.value); // Case-sensitive operation.
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, key);
    }
}
