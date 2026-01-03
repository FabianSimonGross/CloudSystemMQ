package de.nimzan.node.resources;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class PortManager {

    private static final int START_PORT = 30000;
    private static final int MAX_PORT   = 31000;

    /** Ports currently free for allocation */
    private final Queue<Integer> freePorts = new ConcurrentLinkedQueue<>();

    public PortManager() {
        for (int p = START_PORT; p <= MAX_PORT; p++) {
            freePorts.add(p);
        }
    }

    /**
     * Allocate a free port.
     */
    public int allocate() {
        Integer port = freePorts.poll();
        if (port == null) {
            throw new IllegalStateException(
                    "No free ports available (" + START_PORT + "-" + MAX_PORT + ")"
            );
        }
        return port;
    }

    /**
     * Release a port back to the pool.
     */
    public void release(int port) {
        if (port < START_PORT || port > MAX_PORT) return;

        // Prevent double-release duplicates
        if (!freePorts.contains(port)) {
            freePorts.offer(port);
        }
    }
}