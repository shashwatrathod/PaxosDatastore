package server.dto;

import server.server.Operation;

import java.io.Serializable;

public class Promise implements Serializable {
    private final long proposalId;
    private final int acceptorId;
    private final AcceptAck acceptAck;

    public Promise(long proposalId, int acceptorId, AcceptAck acceptAck) {
        this.proposalId = proposalId;
        this.acceptorId = acceptorId;
        this.acceptAck = acceptAck;
    }

    public Promise(long proposalId, int acceptorId) {
        this(proposalId, acceptorId, null);
    }

    public long getProposalId() {
        return proposalId;
    }

    public int getAcceptorId() {
        return acceptorId;
    }

    public AcceptAck getAcceptAck() {
        return acceptAck;
    }
}
