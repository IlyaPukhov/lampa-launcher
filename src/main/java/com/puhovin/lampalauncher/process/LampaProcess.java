package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Launches Lampa application and waits until it is closed
 */
public class LampaProcess {
    private static final Logger LOGGER = Logger.getLogger(LampaProcess.class.getName());

    private final Config config;
    public Process process;

    public LampaProcess(Config config) {
        this.config = config;
    }

    public void start() throws IOException {
        process = new ProcessBuilder(config.lampaPath().toString())
                .start();
        LOGGER.info("Lampa started");
    }

    public void waitForExit() throws InterruptedException {
        if (process != null) {
            process.waitFor();
        }
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
            LOGGER.info("Lampa stopped");
        }
    }
}

