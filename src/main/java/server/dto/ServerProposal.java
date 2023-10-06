package server.dto;

import server.server.Operation;

public class ServerProposal extends Proposal {
    private final Operation operation;

    public ServerProposal(long proposalId, int proposerId, int logPosition, Operation operation) {
        super(proposalId, proposerId, logPosition);
        this.operation = operation;
    }

    public Operation getOperation() {
        return operation;
    }
}
