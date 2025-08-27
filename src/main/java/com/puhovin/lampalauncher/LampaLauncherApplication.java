package com.puhovin.lampalauncher;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.config.LauncherConfigLoader;
import com.puhovin.lampalauncher.exception.LauncherConfigurationException;
import com.puhovin.lampalauncher.exception.LauncherException;
import com.puhovin.lampalauncher.process.ProcessManager;
import com.puhovin.lampalauncher.validation.EnvironmentValidator;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * Application entry point for Lampa Launcher.
 * <p>
 * Loads configuration, validates environment, starts TorrServer and Lampa,
 * waits for Lampa exit, and ensures safe shutdown.
 */
@Slf4j
public class LampaLauncherApplication {

    private static final Path CONFIG_FILE = Path.of("./launcher.properties");

    public static void main(String[] args) {
        ProcessManager processManager = null;

        try {
            Config config = new LauncherConfigLoader(CONFIG_FILE).getConfig();
            new EnvironmentValidator(config).validateEnvironment();

            processManager = new ProcessManager(config);
            processManager.startAll();

            log.info("All processes started successfully. Waiting for Lampa to exit...");
            processManager.waitForLampaExit();

        } catch (LauncherConfigurationException e) {
            log.error("Configuration error: {}", e.getMessage(), e);
            System.exit(3);
        } catch (LauncherException e) {
            log.error("Launcher error: {}", e.getMessage(), e);
            System.exit(2);
        } catch (Exception e) {
            log.error("Unexpected failure during launcher execution", e);
            System.exit(1);
        } finally {
            if (processManager != null) {
                try {
                    processManager.shutdown();
                } catch (Exception e) {
                    log.warn("Error during final shutdown: {}", e.getMessage(), e);
                }
            }
        }
    }
}
