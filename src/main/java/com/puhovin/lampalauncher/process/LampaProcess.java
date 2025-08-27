package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Manages Lampa external process.
 */
@Slf4j
@RequiredArgsConstructor
public class LampaProcess implements ManagedProcess {

    private final Config config;
    private Process process;

    @Override
    public void start() throws IOException {
        if (process != null && process.isAlive()) {
            log.warn("Lampa already running");
            return;
        }

        ProcessBuilder processBuilder = new ProcessBuilder(config.lampaPath().toString());
        processBuilder.redirectErrorStream(false);

        log.info("Starting Lampa...");
        process = processBuilder.start();
        log.info("Lampa started (pid={})", process.pid());

        attachProcessStreamReaders(process, log, "lampa");
    }

    @Override
    public void stop() {
        if (process != null && process.isAlive()) {
            log.info("Stopping Lampa...");
            process.destroy();
            log.info("Lampa stopped");
        }
    }

    @Override
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * Blocks until the process exits.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public void waitForExit() throws InterruptedException {
        if (process != null) {
            process.waitFor();
        }
    }
}
