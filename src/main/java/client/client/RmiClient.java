package client.client;

import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.datastore.DataStore;
import server.paxos.ClientProposer;
import server.server.ServerRemote;

public class RmiClient {

  private final String name;
  private final String host;
  private final int port;

  /**
   * Initializes an RMI client.
   * @param name name of the remote object
   */
  public RmiClient(String name, String host, int port) {
    if (name == null || host == null) {
      throw new IllegalArgumentException("The name is null.");
    }

    this.host = host;
    this.port = port;
    this.name = name;
  }

  /**
   * Looks for the remote object in the registry and delegates tasks to the controller.
   */
  public void start() {
    try {
      Registry registry = LocateRegistry.getRegistry(this.host, this.port);

      ClientProposer clientProposer = (ClientProposer) registry.lookup(this.name);

      new ClientDatastoreController( clientProposer, new InputStreamReader(System.in), System.out).start();
    } catch (RemoteException | NotBoundException e) {
      System.out.print("Either the name is not bound to any object or there was a problem finding the registry: ");
      System.out.println(e.getMessage());
    }
  }


}
