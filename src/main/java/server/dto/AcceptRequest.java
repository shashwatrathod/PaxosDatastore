package server.dto;

import server.server.Operation;

import java.io.Serializable;

public class AcceptRequest implements Serializable {
    private final long proposalId;
    private final int proposerId;
    private final int logPosition;
    private final Operation operation;


    public AcceptRequest(long proposalId, int proposerId, int logPosition, Operation operation) {
        this.proposalId = proposalId;
        this.proposerId = proposerId;
        this.logPosition = logPosition;
        this.operation = operation;
    }

    public long getProposalId() {
        return proposalId;
    }

    public int getProposerId() {
        return proposerId;
    }

    public int getLogPosition() {
        return logPosition;
    }

    public Operation getOperation() {
        return operation;
    }
}
