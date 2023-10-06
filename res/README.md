# Key-Value Store

## Overview

The project features a Key-Value data store that takes in an integer as a key and a string as value.
This data store can be manipulated and queried by a remote client by remotely invoking methods on
the objects bound to the registry by our server. To support RPC, the server and client use Java's
RMI. Currently, the server supports 3 operations on the data store:

- `PUT, <key>, <value>` - Creates a new entry in the datastore for the key, or updates the current
  one with the new value if `key` already exists.
- `GET, <key>` - Returns the value of the key if such a key exists in the datastore.
- `DELETE, <key>` - Deletes the entry pertaining to the `key` if such a key exists in the datastore.

The purpose of this project was to bolster one's understanding of how RPC works, as well as develop
a system that is resistant against failures. I believe this purpose has been fulfilled. While
developing this project, I quickly realized that there are many minute details that need to be taken
into consideration before we can come up with a concrete solution. Developing this project has
provided me a short introduction to what developing intricate backends for production apps may look
like.

## Technical Impressions

The project is developed using Java and Java's RMI. After researching about both, RMI and Apache
Thrift, I realised that while Apache Thrift provides much greater flexibility, this project doesn't
require the granular control offered by Thrift. Therefore, I decided to use RMI which is included in
the JDK. \
The following sections tour through the specific functionalities of each component:

### DataStore

The datastore is an entity that stores the key-value pairs and performs CRUD operations on it.

`DataStore.java`: The DataStore interface provides signature of the `get`, `put`, and `delete`
methods that any class implementing this interface must override. The datastore accepts two
generics: `K` denoting the type of key and `V` denoting the type of value. This interface also
extends RMI's `Remote` interface. Which means that any object of type DataStore can be bound to a
registry.

`IntegerStringDataStore.java`: This represents an implementation of the `DataStore` where the keys
are `Integer` and the values are `String`. Since it holds some data much like a database, I decided
to make this class singleton; i.e. only one instance of this class can be made. This instance can be
fetched by using the `instance()` method. Internally, it uses a map to store the actual data.
Specifically, it uses a `ConcurrentHashMap` so that in a case where multiple clients are attempting
to access/modify the data, the integrity of our data is maintained. I decided to use
a `ConcurrentHashMap` instead of explicitly synchronizing because I realised that the only
operations that affect the integrity of the data are the ones performed on the Map.

### Server

The server module initializes the server objects and is used to create a server.

`RmiServer.java`: The constructor of our RMI Server takes in two parameters, namely, `name`
and `port`. It also contains a `start` method. Upon executing the start method, the server creates a
registry at the given `port` and binds our `DataStore` remote object to the given `name`.
The `start` method catches any `RemoteException`s that may occur during the execution of the program
and prints out the message to the console.

### Client

THe client module initializes the client, locates a registry and finds the remote object.

`RmiClient.java`: The constructor of our RMI client takes in three parameters, namely, `name`
, `host`, and `port`. It also contains a `start()` method. Once this method is called, the client
locates a registry using the given `host` and `port`. Once this registry is found, it looks up the
remote object bound to `name` in the registry. Then it delegates the rest of the tasks
to `ClientController`. The client also catches any exceptions that may occur due to the registry not
being found or the object nou being bound to the given `name` and prints out an error message to the
console.

### Client Controller

The client controller is responsible for taking inputs from the user, processing the inputs, running
basic validations and calling appropriate methods on the `DataStore` remote object. The constructor
of our `ClientDataStoreController` accepts three arguments: a `DataStore` object, a `Readable` to
read inputs from, and an `Appendable` to print the results to. After calling the start method, our
controller first shows a welcome message that displays a list of possible commands and then begins
listening for user inputs. If the command is `q`, the controller finishes it's execution and the
program is terminated. Otherwise, the command is processed to check if it is a valid one, and if it
is, appropriate `DataStore` methods are called. This task is performed using the helper
methods `processGet`, `processPut` and `processDelete`. If the command is invalid, the controller
will try to be as specific as it can in pointing out the errors in the user's command and will
prompt the user to re-enter the command.

## How To Use This Program

Following is the list of valid commands. All the commands are case-insensitive. Each parameter in
the command is seperated by a ",".

1. `PUT, <key>, <value>`, e.g. `put, 3, three`
2. `GET, <key>`, e.g. `get, 3`
3. `DELETE, <key>` e.g. `delete, 3`

Here, the keys must be valid integers. The values are strings.

If the command is invalid, the controller will try to respond with an error message as specific as
possible to let the user know what part of the command is malformed, else it will respond with a
message denoting successful execution of the command.

After booting up the client, the client will prompt the user for inputs and print out the responses
to the standard output.

The server pre-populates the `DataStore` with the following key-value pairs:

- 1 -> one
- 2 -> two
- 3 -> three
- 4 -> four
- 5 -> five

To run the .jar files, navigate to /res.

### Running Server

The server takes in 2 optional CLI arguments in any order:

1. `-p <port number>`: the port number at which the registry is to be created. The port number must be a number in the
   range [1024, 65535].
2. `-n <name>`: name to which the remote `DataStore` is to be bound to.

If the arguments are not provided, the program will use default values and proceed with execution.
The server may crash if the provided port is already in use.

Example usage:

```
java -jar Server.jar -p 4567 -n DataStore
```

The above command will start a server and create a registry bound to port 4567, and bind the data
store to the name "DataStore".

### Running Client

The client takes in 3 optional CLI arguments in any order:

1. `-p <port number>`: the port number at which the registry is to be found. The port number must be a number in the
   range [1024, 65535].
2. `-h <host name>`: name of the host
3. `-n <name>`: name to which the remote `DataStore` is bound to.

If the arguments are not provided, the program will use default values and proceed with execution.
The client may crash if the provided port is already in use or if the host is invalid.

Example usage:

```
java -jar Client.jar -h localhost -p 4567 -n DataStore
```

## Example Commands

Following are some valid example commands that you can try:

- `put, 2, two`
- `put, 5, five`
- `delete, 2`
- `get, 5`
- `delete, 5`

## Citations

- https://docs.oracle.com/en/java/
