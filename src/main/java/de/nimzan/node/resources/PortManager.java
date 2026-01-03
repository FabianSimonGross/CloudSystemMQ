package de.nimzan.node.resources;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class PortManager {

    private static final int START_PORT = 30000;
    private static final int MAX_PORT   = 31000;

    private final ConcurrentLinkedDeque<Integer> freePorts = new ConcurrentLinkedDeque<>();
    private final Set<Integer> allocatedPorts = ConcurrentHashMap.newKeySet();

    public PortManager() {
        for (int p = START_PORT; p <= MAX_PORT; p++) {
            freePorts.add(p);
        }
    }

    public int allocate() {
        Integer port = freePorts.pollFirst();
        if (port == null) {
            throw new IllegalStateException(
                    "No free ports available (" + START_PORT + "-" + MAX_PORT + ")"
            );
        }
        allocatedPorts.add(port);
        return port;
    }

    public void release(int port) {
        if (port < START_PORT || port > MAX_PORT) return;
        if (allocatedPorts.remove(port)) { // only returns true if it was actually allocated
            freePorts.offerLast(port);
        }
    }
}