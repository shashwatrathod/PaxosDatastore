package server.paxos;

import server.dto.Promise;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerProposer extends Remote {
    /**
     * Send a promise to this proposer. If the promise contains a piggybacked value, upon receipt of majority promises, this
     * proposer will send out the piggybacked value as proposed value to acceptors, else it will send its own predetermined value.
     */
    void promise(Promise promise) throws RemoteException;

    int getProposerId() throws RemoteException;
}