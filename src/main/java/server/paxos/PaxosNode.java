package server.paxos;

import java.rmi.RemoteException;

public interface PaxosNode extends Acceptor, Learner, ServerProposer, ClientProposer {

    /**
     * Register a remote PAXOS node. This PAXOS node can then subsequently start communicating with the newly added node.
     */
    void registerPaxosNode(String host, String name, int port) throws RemoteException;
}
