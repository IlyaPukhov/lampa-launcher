package com.puhovin.lampalauncher.process;

import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lifecycle contract for a managed external process.
 * <p>
 * Methods are intentionally minimal: start, stop, isAlive.
 */
public interface ManagedProcess {

    /**
     * Shared executor for background stream readers of all managed processes.
     */
    ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /**
     * Attaches asynchronous readers to process stdout and stderr streams.
     * Output is redirected to SLF4J logger with per-process markers.
     *
     * @param process process to attach readers to
     * @param logger  logger instance to use
     * @param name    process name (used for log discrimination)
     */
    default void attachProcessStreamReaders(Process process, Logger logger, String name) {
        EXECUTOR.submit(new StreamGobbler(process.getInputStream(), logger, name, false)); // stdout
        EXECUTOR.submit(new StreamGobbler(process.getErrorStream(), logger, name, true));  // stderr
    }

    /**
     * Starts the process.
     *
     * @throws IOException if the process cannot be started
     */
    void start() throws IOException;

    /**
     * Requests the process to stop.
     * Implementations should attempt graceful shutdown before forcing termination.
     */
    void stop();

    /**
     * Checks if the process is currently alive.
     *
     * @return {@code true} if the process is alive, {@code false} otherwise
     */
    boolean isAlive();
}
