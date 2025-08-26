package com.puhovin.lampalauncher;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.config.LauncherConfigLoader;
import com.puhovin.lampalauncher.exception.LauncherConfigurationException;
import com.puhovin.lampalauncher.exception.LauncherException;
import com.puhovin.lampalauncher.process.ProcessManager;
import com.puhovin.lampalauncher.validation.EnvironmentValidator;
import lombok.extern.slf4j.Slf4j;

/**
 * Application entry point for Lampa Launcher.
 *
 * <p>This class:
 * <ul>
 *   <li>Loads configuration</li>
 *   <li>Validates the environment</li>
 *   <li>Starts TorrServer and Lampa via ProcessManager</li>
 *   <li>Waits for Lampa exit and performs graceful shutdown</li>
 * </ul>
 */
@Slf4j
public class LampaLauncherApplication {

    public static void main(String[] args) {
        ProcessManager processManager = null;

        try {
            // Load config
            LauncherConfigLoader configLoader = new LauncherConfigLoader();
            Config config = configLoader.getConfig();

            // Validate environment
            EnvironmentValidator validator = new EnvironmentValidator(config);
            validator.validateEnvironment();

            // Start processes
            processManager = new ProcessManager(config);
            processManager.startTorrServer();
            processManager.startLampa();

            log.info("All processes started successfully. Waiting for Lampa to exit...");

            processManager.waitForLampaExit();

            log.info("Lampa exited, proceeding to shutdown.");
            processManager.shutdown();

        } catch (LauncherConfigurationException e) {
            log.error("Configuration error: {}", e.getMessage(), e);
            System.exit(2);
        } catch (LauncherException e) {
            log.error("Launcher error: {}", e.getMessage(), e);
            System.exit(3);
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
