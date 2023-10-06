package server.dto;

import server.server.Operation;

import java.io.Serializable;

public class AcceptAck implements Serializable {
    private final int logPosition;
    private final int acceptorId;
    private final Operation operation;

    private final long proposalId;

    public AcceptAck(int logPosition, int acceptorId, Operation operation, long proposalId) {
        this.logPosition = logPosition;
        this.acceptorId = acceptorId;
        this.operation = operation;
        this.proposalId = proposalId;
    }

    public AcceptAck(int acceptorId, AcceptRequest acceptRequest) {
        this(acceptRequest.getLogPosition(), acceptorId, acceptRequest.getOperation(), acceptRequest.getProposalId());
    }

    public int getLogPosition() {
        return logPosition;
    }

    public int getAcceptorId() {
        return acceptorId;
    }

    public Operation getOperation() {
        return operation;
    }

    public long getProposalId() {
        return proposalId;
    }
}
