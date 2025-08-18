package com.puhovin.lampalauncher;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.process.LampaProcess;
import com.puhovin.lampalauncher.process.TorrServerDaemon;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts TorrServer, launches Lampa, and shuts down TorrServer after Lampa is closed.
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            return;
        }

        Config config = new Config(
                Path.of(props.getProperty("torrserver.path")),
                Integer.parseInt(props.getProperty("torrserver.port")),
                Path.of(props.getProperty("lampa.path"))
        );

        TorrServerDaemon torrServer = new TorrServerDaemon(config);
        LampaProcess lampa = new LampaProcess(config);

        try {
            torrServer.start();
            torrServer.waitForPort(Duration.ofSeconds(10));

            lampa.start();
            LOGGER.info("Lampa started. Waiting for user to close it...");

            lampa.waitForExit();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Main thread was interrupted. Exiting.", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "I/O error occurred while starting processes", e);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Application startup failed", e);
        } finally {
            lampa.stop();
            torrServer.stop();
            LOGGER.info("All processes stopped. Application exiting.");
        }
    }
}
