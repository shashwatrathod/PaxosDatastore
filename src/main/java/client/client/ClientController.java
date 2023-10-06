package client.client;

import java.rmi.RemoteException;

public interface ClientController {

    /**
     * Takes inputs from the user and executes the commands on the datastore unless asked to quit.
     */
    void start() throws RemoteException;
}
