package server.dto;

import java.io.Serializable;

public class Proposal implements Serializable {
    private final long proposalId;
    private final int proposerId;
    private final int logPosition;

    public Proposal(long proposalId, int proposerId, int logPosition) {
        this.proposalId = proposalId;
        this.proposerId = proposerId;
        this.logPosition = logPosition;
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
}
