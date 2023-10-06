package server.server;

import java.rmi.RemoteException;

public abstract class AbstractOperation implements Operation {
    protected final Integer key;

    protected AbstractOperation(Integer key) throws RemoteException {
        super();
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }

        this.key = key;
    }

    @Override
    public Integer key() {
        return this.key;
    }
}
