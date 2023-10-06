package client.driver;

import client.client.RmiClient;

public class RmiClientDriver {

    public static void main(String[] args) {

        String host = "";
        String name = "";
        int port = -1;

        int argsLen = args.length;

        for (int i = 0; i < argsLen; i += 2) {
            switch (args[i].toLowerCase()) {
                case "-h":
                    if (i + 1 < argsLen) {
                        host = args[i + 1];
                    }
                    break;
                case "-n":
                    if (i + 1 < argsLen) {
                        name = args[i + 1];
                    }
                    break;
                case "-p":
                    if (i + 1 < argsLen && isInteger(args[i + 1])) {
                        port = Integer.parseInt(args[i + 1]);
                    }
                    break;
                default:
                    break;
            }
        }

        if (host.isBlank() || name.isBlank() || port < 0) {
            System.out.println("Enter valid arguments!");
            return;
        }

        System.out.println("Port: " + port);
        System.out.println("Host: " + host);
        System.out.println("Name: " + name);

        try {
            new RmiClient(name, host, port).start();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static boolean isInteger(String n) {
        try {
            Integer.parseInt(n);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
