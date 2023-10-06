package server.paxos;

import server.dto.AcceptAck;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Learner extends Remote {

    /**
     * Provides an acknowledgement to this learner that a proposal has been accepted by certain acceptor.
     * If a majority of acceptors agree upon the same operation, the operation is executed and related changes inside the datastore are made.
     */
    void accept(AcceptAck acceptAcknowledgement) throws RemoteException;

    String get(Integer key) throws RemoteException;

    int getLearnerId() throws RemoteException;
}
