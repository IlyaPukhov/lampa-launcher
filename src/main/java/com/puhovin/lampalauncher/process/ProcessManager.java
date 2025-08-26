package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.exception.LampaLaunchException;
import com.puhovin.lampalauncher.exception.TorrServerException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Manages Lampa and TorrServer processes.
 * Provides methods to start, stop, and check if processes are alive.
 */
@Slf4j
public class ProcessManager {

    private final Config config;
    private final TorrServerDaemon torrServer;
    private final LampaProcess lampa;

    public ProcessManager(Config config) {
        this.config = config;
        this.torrServer = new TorrServerDaemon(config);
        this.lampa = new LampaProcess(config);
    }

    public void startTorrServer() throws TorrServerException {
        log.info("Starting TorrServer...");
        try {
            torrServer.start();
            Duration timeout = Duration.ofSeconds(config.torrServerStartupTimeout());
            torrServer.waitForPort(timeout);
            log.info("TorrServer is up");
        } catch (Exception e) {
            throw new TorrServerException("Failed to start or wait for TorrServer", e);
        }
    }

    public void startLampa() throws LampaLaunchException {
        log.info("Starting Lampa...");
        try {
            lampa.start();
        } catch (Exception e) {
            throw new LampaLaunchException("Failed to start Lampa", e);
        }
    }

    public void waitForLampaExit() throws InterruptedException {
        lampa.waitForExit();
    }

    /**
     * Requests processes to stop politely. Does not forcibly kill them.
     */
    public void shutdown() {
        stopProcess(lampa, "Lampa");
        stopProcess(torrServer, "TorrServer");
        log.info("Shutdown sequence finished");
    }

    private void stopProcess(ManagedProcess proc, String name) {
        if (proc.isAlive()) {
            log.info("Stopping {} (requested)", name);
            proc.stop();
        }
    }
}
