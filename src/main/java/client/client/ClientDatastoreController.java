package client.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

import server.paxos.ClientProposer;

public class ClientDatastoreController implements ClientController {

    private final ClientProposer clientProposer;
    private final Readable in;
    private final Appendable out;

    public ClientDatastoreController(ClientProposer clientProposer, Readable in, Appendable out) {
        if (clientProposer == null) {
            throw new IllegalArgumentException("The datastore is null.");
        }

        if (in == null) {
            throw new IllegalArgumentException("Readable is null.");
        }

        if (out == null) {
            throw new IllegalArgumentException("The output is null.");
        }

        this.clientProposer = clientProposer;
        this.in = in;
        this.out = out;
    }

    @Override
    public void start() throws RemoteException {
        Scanner scan = new Scanner(in);

        printMessage(welcomeMessage());
        while (true) {
            printMessage("Enter command: ");
            String input = scan.nextLine();

            if (input.equalsIgnoreCase("q")) break;

            String[] commandComponents = input.split(",");

            if (commandComponents.length < 1) {
                printMessage("Command parameters should be seperated by ',', but got: " + input);
            }

            commandComponents = Arrays.stream(commandComponents)
                    .map(String::trim)
                    .collect(Collectors.toList())
                    .toArray(new String[commandComponents.length]);

            String[] commandParameters = Arrays.copyOfRange(commandComponents, 1,
                    commandComponents.length);

            switch (commandComponents[0].toLowerCase()) {
                case "put":
                    processPut(commandParameters);
                    break;
                case "get":
                    processGet(commandParameters);
                    break;
                case "delete":
                    processDelete(commandParameters);
                    break;
                default:
                    printMessage("Unknown command: " + commandComponents[0]);
            }
        }
    }

    private void processPut(String[] commandParameters) {
        if (commandParameters.length != 2) {
            printMessage("Expected exactly 2 parameters <key>, <value> but got: "
                    + Arrays.toString(commandParameters));
            return;
        }

        String sKey = commandParameters[0];
        String value = commandParameters[1];

        if (!isInteger(sKey)) {
            printMessage("The key must be an integer, but got: " + sKey);
            return;
        }

        try {
            int key = Integer.parseInt(sKey);
            this.clientProposer.put(key, value);
            printMessage(String.format("PUT(%d, %s): Sent.", key, value));
        } catch (IllegalArgumentException | RemoteException iae) {
            printMessage("Couldn't perform the PUT operation: " + iae.getMessage());
        }
    }

    private void processGet(String[] commandParameters) throws RemoteException {
        if (commandParameters.length != 1) {
            printMessage("Expected exactly 1 parameter: <key> but got: "
                    + Arrays.toString(commandParameters));
            return;
        }

        String sKey = commandParameters[0];

        if (!isInteger(sKey)) {
            printMessage("The key must be an integer, but got: " + sKey);
            return;
        }

        int key = Integer.parseInt(sKey);

        String value = this.clientProposer.get(key);

        if (value == null) {
            printMessage(String.format("Key %d not found.", key));
        } else {
            printMessage(String.format("Success: GET(%d) = %s", key, value));
        }
    }

    private void processDelete(String[] commandParameters) throws RemoteException {
        if (commandParameters.length != 1) {
            printMessage("Expected exactly 1 parameter: <key> but got: "
                    + Arrays.toString(commandParameters));
            return;
        }

        String sKey = commandParameters[0];

        if (!isInteger(sKey)) {
            printMessage("The key must be an integer, but got: " + sKey);
            return;
        }

        int key = Integer.parseInt(sKey);

        this.clientProposer.delete(key);

        printMessage(String.format("DELETE(%s): Sent.", key));

    }

    private boolean isInteger(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private void printMessage(String message) {
        try {
            this.out.append(message);
            this.out.append("\n");
        } catch (IOException ioe) {
            System.out.println("Encountered a problem while appending: " + ioe.getMessage());
        }
    }

    private String welcomeMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append("Welcome! \n");
        sb.append("You can enter the following case-insensitive commands to perform operations on the datastore: \n");
        sb.append("\t\tPUT, <integer key>, <string value>\n");
        sb.append("\t\tGET, <integer key>\n");
        sb.append("\t\tDELETE, <integer key>\n");
        sb.append("or Enter 'q' to quit\n");

        return sb.toString();
    }
}
