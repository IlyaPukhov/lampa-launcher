package com.puhovin.lampalauncher;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.config.ConfigLoader;

import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Starts TorrServer, launches Lampa, and shuts down TorrServer after Lampa is closed.
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        AppLifecycleManager lifecycleManager = null;
        try {
            LogSetup.init();
            Config config = ConfigLoader.load("config.properties");

            lifecycleManager = new AppLifecycleManager(config);
            lifecycleManager.start();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(SEVERE, "Main thread was interrupted. Exiting.", e);
        } catch (IOException | IllegalStateException e) {
            LOGGER.log(SEVERE, "Application startup failed", e);
        } finally {
            if (lifecycleManager != null) lifecycleManager.stop();
        }
    }
}
