package com.puhovin.lampalauncher.process;

import java.io.IOException;

/**
 * Simple lifecycle contract for managed external processes.
 * <p>
 * Methods are intentionally minimal: start / stop / isAlive.
 */
public interface ManagedProcess {

    /**
     * Starts the process.
     *
     * @throws IOException if the process cannot be started
     */
    void start() throws IOException;

    /**
     * Requests the process to stop.
     * Implementations may attempt to stop the process without killing forcibly.
     */
    void stop();

    /**
     * Returns true if the process is currently running.
     *
     * @return true if alive, false otherwise
     */
    boolean isAlive();
}
