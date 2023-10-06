package server.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents the set of CRUD operations that the client can
 * perform on the datastore.
 */
public interface ServerRemote extends Remote {
    String get(Integer key) throws RemoteException;

    boolean put(Integer key, String value) throws RemoteException;

    boolean delete(Integer key) throws RemoteException;
}
