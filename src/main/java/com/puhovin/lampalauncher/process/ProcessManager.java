package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.exception.LampaLaunchException;
import com.puhovin.lampalauncher.exception.TorrServerLaunchException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.stream.Stream;

/**
 * Manages Lampa and TorrServer processes.
 * Provides methods to start, stop, and check if processes are alive.
 */
@Slf4j
public class ProcessManager {

    private final Config config;
    private final TorrServerDaemonProcess torrServer;
    private final LampaProcess lampa;

    public ProcessManager(Config config) {
        this.config = config;
        this.torrServer = new TorrServerDaemonProcess(config);
        this.lampa = new LampaProcess(config);
    }

    public void startAll() throws TorrServerLaunchException, LampaLaunchException {
        startTorrServer();
        startLampa();
    }

    private void startTorrServer() throws TorrServerLaunchException {
        try {
            torrServer.start();
            Duration timeout = Duration.ofSeconds(config.torrServerStartupTimeout());
            torrServer.waitForPort(timeout);
            log.info("TorrServer is up");
        } catch (Exception e) {
            throw new TorrServerLaunchException("Failed to start TorrServer", e);
        }
    }

    private void startLampa() throws LampaLaunchException {
        try {
            lampa.start();
        } catch (Exception e) {
            throw new LampaLaunchException("Failed to start Lampa", e);
        }
    }

    public void waitForLampaExit() throws InterruptedException {
        lampa.waitForExit();
    }

    public void shutdown() {
        Stream.of(lampa, torrServer)
                .filter(ManagedProcess::isAlive)
                .forEach(ManagedProcess::stop);
        log.info("Shutdown sequence finished");
    }
}
