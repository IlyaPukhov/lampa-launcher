package com.puhovin.lampalauncher;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.process.LampaProcess;
import com.puhovin.lampalauncher.process.TorrServerDaemon;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Logger;

public class AppLifecycleManager {
    private static final Logger LOGGER = Logger.getLogger(AppLifecycleManager.class.getName());

    private final TorrServerDaemon torrServer;
    private final LampaProcess lampa;

    public AppLifecycleManager(Config config) {
        this.torrServer = new TorrServerDaemon(config);
        this.lampa = new LampaProcess(config);
    }

    public void start() throws IOException, InterruptedException {
        torrServer.start();
        torrServer.waitForPort(Duration.ofSeconds(10));

        lampa.start();
        LOGGER.info("Lampa started. Waiting for user to close it...");
        lampa.waitForExit();
    }

    public void stop() {
        lampa.stop();
        torrServer.stop();
        LOGGER.info("All processes stopped.");
    }
}

