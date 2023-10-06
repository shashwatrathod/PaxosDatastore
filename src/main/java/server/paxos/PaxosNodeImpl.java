package server.paxos;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.datastore.ConcurrentDataStoreImpl;
import server.datastore.DataStore;
import server.dto.*;
import server.server.DeleteOperation;
import server.server.Operation;
import server.server.PutOperation;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PaxosNodeImpl implements PaxosNode {

    private final DataStore<Integer, String> dataStore;

    // Proposals sent by proposer to acceptors indexed by ID
    private final Map<Long, ServerProposal> proposalsSentById;

    // Promises for a log position received by the proposer
    private final Map<Integer, List<Promise>> promisesReceivedForLogPosition;

    // Proposals received by an acceptor
    private final Map<Integer, Proposal> proposalsReceivedForLogPosition;

    // Accepted acks received by learner for a proposer
    private final Map<Integer, List<AcceptAck>> acceptAcksForLogPosition;

    // Acks for which the learner performed the requested operation.
    private final Map<Integer, AcceptAck> acceptedAcknowledgements;

    // Proposals indexed by log position accepted by the acceptor.
    private final Map<Integer, AcceptRequest> proposalsAccepted;

    private final Map<Integer, ServerProposer> proposersById;

    private final Map<Integer, Learner> learnersById;

    private final Map<Integer, Acceptor> acceptorsById;

    private int currentLogPosition;
    private final int acceptorId;
    private final int proposerId;
    private final int learnerId;

    private final Logger ACCEPTOR_LOGGER;
    private final Logger PROPOSER_LOGGER;
    private final Logger LEARNER_LOGGER;
    private final Logger PAXOS_LOGGER;

    public PaxosNodeImpl() {
        this.dataStore = new ConcurrentDataStoreImpl<>();
        this.dataStore.put(1, "one");
        this.dataStore.put(2, "two");
        this.dataStore.put(3, "three");
        this.dataStore.put(4, "four");
        this.dataStore.put(5, "five");
        this.proposalsSentById = new ConcurrentHashMap<>();
        this.promisesReceivedForLogPosition = new ConcurrentHashMap<>();
        this.proposalsReceivedForLogPosition = new ConcurrentHashMap<>();
        this.acceptAcksForLogPosition = new ConcurrentHashMap<>();
        this.proposalsAccepted = new ConcurrentHashMap<>();
        this.acceptedAcknowledgements = new ConcurrentHashMap<>();
        this.currentLogPosition = 0;

        int maxBound = 999999;
        int minBound = 1000;

        int randomId = getRandomIntBetween(maxBound, minBound);
        this.acceptorId = randomId;
        this.proposerId = randomId;
        this.learnerId = randomId;

        this.acceptorsById = new HashMap<>();
        // Also need to add self as an acceptor
        this.acceptorsById.put(this.acceptorId, this);

        this.proposersById = new HashMap<>();
        // Also need to add self as a proposer
        this.proposersById.put(this.proposerId, this);

        this.learnersById = new HashMap<>();
        // Also need to add self as a learner
        this.learnersById.put(this.learnerId, this);

        ACCEPTOR_LOGGER = LogManager.getLogger(String.format("Acceptor@%s", acceptorId));
        LEARNER_LOGGER = LogManager.getLogger(String.format("Learner@%s", learnerId));
        PROPOSER_LOGGER = LogManager.getLogger(String.format("Proposer@%s", proposerId));
        PAXOS_LOGGER = LogManager.getLogger("PaxosNode");
    }

    @Override
    public void propose(Proposal proposal) throws RemoteException {

        ACCEPTOR_LOGGER.info(String.format("Proposal %s: Received for Log #%s",
                proposal.getProposalId(), proposal.getLogPosition()));

        // Returning default to save writing unnecessary if-else
        Proposal previousProposal = proposalsReceivedForLogPosition.getOrDefault(proposal.getLogPosition(), proposal);

        if (proposal.getProposalId() <= previousProposal.getProposerId()) {
            ACCEPTOR_LOGGER.info("Proposal %s: Failed; Another proposal with higher ID received.");
            return;
        }

        // Now we know that this is the Max ID for this log position that we have seen

        if (proposalsAccepted.containsKey(proposal.getLogPosition())) {
            // Since we've already accepted a proposal, we want to piggyback the value
            AcceptRequest acceptedProposal = proposalsAccepted.get(proposal.getLogPosition());

            ACCEPTOR_LOGGER.info(String.format("Proposal %s: Piggybacking value to proposal before sending.", proposal.getProposalId()));

            System.out.printf("Proposal %s: - Enter 'spr' to send to promise to proposer or 'ipr' to ignore proposal.%n", proposal.getProposalId());
            Scanner sc = new Scanner(System.in);

            while (true) {
                String s = sc.next();
                if (s.equalsIgnoreCase("spr")) break;
                if (s.equalsIgnoreCase("ipr")) return;
            }

            // We can safely say that this proposal is going to be accepted.
            this.proposalsReceivedForLogPosition.put(proposal.getLogPosition(), proposal);

            Promise promise = new Promise(proposal.getProposalId(),
                    this.acceptorId,
                    new AcceptAck(this.acceptorId, acceptedProposal));


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        proposersById.get(proposal.getProposerId()).promise(promise);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } else {

            System.out.printf("Proposal %s: - Enter 'spr' to send to promise to proposer or 'ipr' to ignore proposal.%n", proposal.getProposalId());
            Scanner sc = new Scanner(System.in);

            while (true) {
                String s = sc.next();
                if (s.equalsIgnoreCase("spr")) break;
                if (s.equalsIgnoreCase("ipr")) return;
            }

            // We can safely say that this proposal is going to be accepted.
            this.proposalsReceivedForLogPosition.put(proposal.getLogPosition(), proposal);

            // We know that we have not accepted any proposals for the log position yet. So we just send back the promise
            ACCEPTOR_LOGGER.info(String.format("Proposal %s: Promising", proposal.getProposalId()));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        proposersById.get(proposal.getProposerId())
                                .promise(new Promise(proposal.getProposalId(), acceptorId));
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    @Override
    public void accept(AcceptRequest acceptRequest) throws RemoteException {
        ACCEPTOR_LOGGER.info(String.format("Proposal %s: Accept Request received", acceptRequest.getProposalId()));
        if (!this.proposalsReceivedForLogPosition.containsKey(acceptRequest.getLogPosition())) {
            ACCEPTOR_LOGGER.info(String.format("Proposal %s: Accept request received for a proposal which was not promised",
                    acceptRequest.getProposerId()));
            return;
        }

        if (acceptRequest.getProposalId() < this.proposalsReceivedForLogPosition
                .get(acceptRequest.getLogPosition())
                .getProposalId()) {
            ACCEPTOR_LOGGER.info(String.format("Proposal %s: Accept rejected; promised another proposal with higher ID",
                    acceptRequest.getProposalId()));
            return;
        }

        // Now we are certain that we have to accept this request.

        AcceptAck acceptAck = new AcceptAck(this.acceptorId, acceptRequest);

        System.out.printf("Proposal %s: - Enter 'sack' to send to accept ACKs to learners or 'iack' to ignore accept request.%n", acceptRequest.getProposalId());
        Scanner sc = new Scanner(System.in);

        while (true) {
            String s = sc.next();
            if (s.equalsIgnoreCase("sack")) break;
            if (s.equalsIgnoreCase("iack")) return;
        }

        // Mark this request as accepted
        this.proposalsAccepted.put(acceptRequest.getLogPosition(), acceptRequest);

        // Send accept ack to each learner
        for (Learner learner : learnersById.values()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ACCEPTOR_LOGGER.info(String.format("Proposal %s: Sending Accept ACK to learner %s",
                                acceptRequest.getProposalId(), learner.getLearnerId()));
                        learner.accept(acceptAck);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

    }

    @Override
    public int getAcceptorId() throws RemoteException {
        return this.acceptorId;
    }

    @Override
    public void put(Integer key, String value) throws RemoteException {
        Operation operation = new PutOperation(key, value);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendNewProposalForOperation(operation);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Override
    public void delete(Integer key) throws RemoteException {
        Operation operation = new DeleteOperation(key);
        this.sendNewProposalForOperation(operation);
    }

    @Override
    public void accept(AcceptAck acceptAcknowledgement) throws RemoteException {
        LEARNER_LOGGER.info(String.format("Proposal %s: Received ACK from %s",
                acceptAcknowledgement.getProposalId(), acceptAcknowledgement.getAcceptorId()));

        // If we have already performed the operation, we don't want to update it again.
        if (this.acceptedAcknowledgements.containsKey(acceptAcknowledgement.getLogPosition())) return;

        appendToMapKey(this.acceptAcksForLogPosition, acceptAcknowledgement.getLogPosition(), acceptAcknowledgement);

        List<AcceptAck> acks = this.acceptAcksForLogPosition.get(acceptAcknowledgement.getLogPosition());

        int majorityNumber = this.acceptorsById.size() / 2 + 1;

        if (acks.size() < majorityNumber) return;

        // We have now received a response from majority. So we can execute the operation.
        acceptAcknowledgement.getOperation().execute(this.dataStore);

        this.acceptedAcknowledgements.put(acceptAcknowledgement.getLogPosition(), acceptAcknowledgement);

        LEARNER_LOGGER.info(String.format("Proposal %s: Operation %s executed for log position %s",
                acceptAcknowledgement.getProposalId(), acceptAcknowledgement.getOperation().toString(), acceptAcknowledgement.getLogPosition()));

    }

    @Override
    public String get(Integer key) throws RemoteException {
        return this.dataStore.get(key);
    }

    @Override
    public int getLearnerId() throws RemoteException {
        return this.learnerId;
    }

    @Override
    public void promise(Promise promise) throws RemoteException {
        PROPOSER_LOGGER.info(String.format("Proposal %s: Received promise from Acceptor %s",
                promise.getProposalId(), promise.getAcceptorId()));

        ServerProposal proposal = proposalsSentById.get(promise.getProposalId());

        appendToMapKey(promisesReceivedForLogPosition, proposal.getLogPosition(), promise);

        List<Promise> promises = this.promisesReceivedForLogPosition.get(proposal.getLogPosition());
        // Now we have to check if we got majority or not.
        int majorityNumber = this.acceptorsById.size() / 2 + 1;
        int numberOfPromises = promises.size();

        if (numberOfPromises < majorityNumber) return;

        Operation operationToBeSent = proposal.getOperation();

        // Now that we have attained majority, we want to check if any promises has a value piggybacked to it.
        // If there is a value piggybacked to it, we will find the value with the highest accepted ID.
        if (promises.stream().anyMatch(p -> p.getAcceptAck() != null)) {
            Promise promiseWithMaxAcceptedId = findAcceptedPromiseWithHighestId(promises);

            operationToBeSent = promiseWithMaxAcceptedId.getAcceptAck().getOperation();
            PROPOSER_LOGGER.info(String.format("Proposal %s: Found piggybacked operation %s",
                    proposal.getProposalId(), operationToBeSent.toString()));
        }

        // Send an accept request to all acceptors
        AcceptRequest acceptRequest = new AcceptRequest(proposal.getProposalId(),
                this.proposerId,
                proposal.getLogPosition(),
                operationToBeSent);

        for (Acceptor acceptor : acceptorsById.values()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PROPOSER_LOGGER.info(String.format("Proposal %s: Accept request sent to Acceptor %s",
                                proposal.getProposalId(), acceptor.getAcceptorId()));
                        acceptor.accept(acceptRequest);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        this.promisesReceivedForLogPosition.get(proposal.getLogPosition()).clear();
    }

    @Override
    public int getProposerId() throws RemoteException {
        return this.proposerId;
    }

    @Override
    public void registerPaxosNode(String host, String name, int port) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            PaxosNode paxosNode = (PaxosNode) registry.lookup(name);

            this.acceptorsById.put(paxosNode.getAcceptorId(), paxosNode);
            this.proposersById.put(paxosNode.getProposerId(), paxosNode);
            this.learnersById.put(paxosNode.getLearnerId(), paxosNode);

            PAXOS_LOGGER.info(String.format("Registered remote paxos node %s:%s", host, port));

        } catch (RemoteException | NotBoundException e) {
            PAXOS_LOGGER.error(String.format("Couldn't add %s:%d : %s", host, port, e.getMessage()));
        }
    }

    private int getRandomIntBetween(int max, int min) {
        Random r = new Random();

        return r.nextInt(max - min) + min;
    }

    private <K, V> void appendToMapKey(Map<K, List<V>> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }

        map.get(key).add(value);
    }

    private Promise findAcceptedPromiseWithHighestId(List<Promise> promises) {
        return promises
                .stream()
                .reduce(
                        new Promise(0, 0,
                                new AcceptAck(0, 0, null, 0)),
                        (maxPromise, currPromise) -> {
                            if (currPromise.getAcceptAck() == null) return maxPromise;

                            if (maxPromise.getAcceptAck().getProposalId() < currPromise.getAcceptAck().getProposalId()) {
                                return currPromise;
                            } else {
                                return maxPromise;
                            }
                        });
    }

    private void sendNewProposalForOperation(Operation operation) throws RemoteException {
        ServerProposal proposal = new ServerProposal(System.nanoTime(), this.proposerId, currentLogPosition, operation);

        this.proposalsSentById.put(proposal.getProposalId(), proposal);

        PROPOSER_LOGGER.info(String.format("Proposal %s: for operation %s and log position %s created",
                proposal.getProposalId(), proposal.getOperation().toString(), proposal.getLogPosition()));

        currentLogPosition++;

        System.out.println("Proposal %s: Proposal - Enter 'sp' to send to acceptors or 'ip' to ignore.");
        Scanner sc = new Scanner(System.in);

        while (true) {
            String s = sc.next();
            if (s.equalsIgnoreCase("sp")) break;
            if (s.equalsIgnoreCase("ip")) return;
        }


        for (Acceptor acceptor : acceptorsById.values()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PROPOSER_LOGGER.info(String.format("Proposal %s: Sent to Acceptor %s",
                                proposal.getProposalId(), acceptor.getAcceptorId()));
                        acceptor.propose(proposal);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }
}
