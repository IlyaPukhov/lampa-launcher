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
 *   <li>Waits for Lampa exit and performs shutdown</li>
 * </ul>
 */
@Slf4j
public class LampaLauncherApplication {

    public static void main(String[] args) {
        ProcessManager processManager = null;

        try {
            Config config = new LauncherConfigLoader().getConfig();
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
