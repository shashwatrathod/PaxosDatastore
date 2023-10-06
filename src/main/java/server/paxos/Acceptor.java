package server.paxos;

import server.dto.AcceptRequest;
import server.dto.Proposal;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Acceptor extends Remote {

    /**
     * Used by Proposers to put up a proposal to this acceptor.
     */
    void propose(Proposal proposal) throws RemoteException;

    /**
     * Instruct this Acceptor to accept a value provided by a proposer for a particular proposal.
     * The request gets rejected if this acceptor has seen another proposal with a higher proposal ID.
     */
    void accept(AcceptRequest acceptRequest) throws RemoteException; // TODO figure out input parameter types

    int getAcceptorId() throws RemoteException;
}
