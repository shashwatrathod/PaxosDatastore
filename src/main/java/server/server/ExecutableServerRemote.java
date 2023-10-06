package server.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a Remote object for the server capable of using 2PC to perform operations
 * on the datastore.
 */
public interface ExecutableServerRemote extends Remote {

    /**
     * Prepares the server to execute the commit. Upon receiving the message,
     * places a lock on the resource about to be modified so that no other thread can change its value.
     * @param operation the operation to be performed
     * @param operationId unique identifier for the operation
     * @return whether the server prepared for the operation. returns false if the resource couldn't be locked.
     */
    boolean prepare(Operation operation, String operationId) throws RemoteException;

    /**
     * Executes the operation with the specified operationId on the datastore. MUST prepare the operation first. Unlocks the resource
     * on which the operation is to be performed before executing.
     * @param operationId unique identifier of the operation to be executed
     * @return whether the operation was executed. Returns false if no such operation is committed or if the resource couldn't be unlocked.
     */
    boolean execute(String operationId) throws RemoteException;

    /**
     * Aborts the commit represented by the given operationId.
     * @param operationId unique identifier for the operation to be aborted
     * @return whether the operation was aborted
     */
    boolean abort(String operationId) throws RemoteException;
}
