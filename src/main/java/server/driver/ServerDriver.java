package server.driver;

import server.paxos.PaxosNode;
import server.paxos.PaxosNodeImpl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ServerDriver {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Please enter correct arguments.");
            return;
        }

        int port = -1;
        String name = "";
        String filepath = "";

        for (int i = 0; i < args.length; i += 2) {
            switch (args[i].toLowerCase()) {
                case "-p":
                    if (ServerDriver.isIntegerParseable(args[i + 1])) {
                        port = Integer.parseInt(args[i + 1]);
                    }
                    break;
                case "-n":
                    name = args[i + 1];
                    break;
                case "-f":
                    filepath = args[i + 1];
                    break;
                default:
                    break;
            }
        }

        if (port == -1 || name.isBlank()) {
            System.out.println("Please enter correct arguments");
            return;
        }

        PaxosNode paxosNode = new PaxosNodeImpl();

        try {
            Registry registry = LocateRegistry.createRegistry(port);
            UnicastRemoteObject.exportObject(paxosNode, 0);
            registry.rebind(name, paxosNode);
            System.out.println(String.format("Exported the remote object %s on port %s", name, port));



            System.out.println("Once all servers are ready, enter hostname, name and port number of other servers on separate lines. " +
                    "Enter 'qe' after you're finished entering.");

            Scanner other = new Scanner(System.in);
            other.next();

            Scanner sc;
            if (filepath.isBlank()) {
                sc = new Scanner(System.in);
            } else {
                Readable rd = new FileReader(filepath);
                sc = new Scanner(rd);
            }

            while (true) {
                String line = sc.nextLine();

                if (line.equalsIgnoreCase("qe")) {
                    break;
                }

                String[] components = line.split(",");

                String hostname = components[0].strip();
                String sName = components[1].strip();
                if (!isIntegerParseable(components[2].strip())) {
                    System.out.println("Enter a valid port!");
                    continue;
                }
                int sPort = Integer.parseInt(components[2].strip());
                paxosNode.registerPaxosNode(hostname, sName, sPort);
            }

            System.out.println("Ready to start operations!");

        } catch (RemoteException e) {
            System.out.println("Remote exception occurred: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Oops! The server crashed: " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find config file.");
        }

    }

    private static boolean isIntegerParseable(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
