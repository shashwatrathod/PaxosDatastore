package server.server;

import org.apache.log4j.Logger;
import server.datastore.LockableDataStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutableServerRemoteImpl implements ExecutableServerRemote {
    private final LockableDataStore<Integer, String> dataStore;
    private final Map<String, Operation> operations;

    private final Logger LOGGER;

    public ExecutableServerRemoteImpl(LockableDataStore<Integer, String> dataStore, Logger LOGGER) {
        if (dataStore == null) {
            throw new IllegalArgumentException("The datastore is null");
        }

        this.dataStore = dataStore;
        this.operations = new ConcurrentHashMap<>();
        this.LOGGER = LOGGER;
    }

    @Override
    public boolean prepare(Operation operation, String operationId) {
        if (operation == null || operationId == null) {
            throw new IllegalArgumentException("Arguments are null");
        }

        this.operations.put(operationId, operation);

        boolean result = this.dataStore.lock(operation.key());
        LOGGER.info(String.format("Prepare operation %s - %s", operationId, result));
        return result;
    }

    @Override
    public boolean execute(String operationId) {
        if (!this.operations.containsKey(operationId)) {
            LOGGER.info(String.format("Execute operation %s - %s; no such operation", operationId, false));
            return false;
        }

        Operation operation = operations.get(operationId);

        if (!this.dataStore.unlock(operation.key())) {
            LOGGER.info(String.format("Execute operation %s - %s; couldn't unlock", operationId, false));
            return false;
        }

        boolean executed = operation.execute(this.dataStore);

        if (executed) {
            operations.remove(operationId);
        }
        LOGGER.info(String.format("Execute operation %s - %s", operationId, executed));

        return executed;
    }

    @Override
    public boolean abort(String operationId) {
        if (!this.operations.containsKey(operationId)){
            LOGGER.info(String.format("Abort operation %s - %s; no such operation", operationId, false));
            return false;
        }

        Operation operation = this.operations.get(operationId);

        this.dataStore.unlock(operation.key());

        this.operations.remove(operationId);
        LOGGER.info(String.format("Abort operation %s - %s", operationId, true));
        return true;
    }
}
