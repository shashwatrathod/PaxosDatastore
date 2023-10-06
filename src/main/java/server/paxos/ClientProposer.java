package server.paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Client-facing part of the server that exposes necessary features offered by the system.
 */
public interface ClientProposer extends Remote {
    String get(Integer key) throws RemoteException;

    void put(Integer key, String value) throws RemoteException;

    void delete(Integer key) throws RemoteException;
}
