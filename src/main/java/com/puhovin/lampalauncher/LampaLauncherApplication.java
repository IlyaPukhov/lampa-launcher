package com.puhovin.lampalauncher;

import com.puhovin.lampalauncher.config.LauncherConfiguration;
import com.puhovin.lampalauncher.process.ProcessManager;
import com.puhovin.lampalauncher.validation.EnvironmentValidator;
import lombok.extern.slf4j.Slf4j;

/**
 * Главный класс приложения Lampa Launcher
 * <p>
 * Этот лаунчер:
 * - Запускает TorrServer как демон процесс
 * - Запускает Lampa приложение
 * - Мониторит здоровье процессов
 * - Корректно завершает все процессы при выходе
 * - Логирует всю активность в файл launcher.log
 */
@Slf4j
public class LampaLauncherApplication {

    private LauncherConfiguration config;
    private EnvironmentValidator validator;
    private ProcessManager processManager;

    public static void main(String[] args) {
        initializeComponents();

        runLauncher();
    }

    private void initializeComponents() throws Exception {
        log.info("Initializing Lampa Launcher...");

        config = new LauncherConfiguration();
        validator = new EnvironmentValidator(config);
        processManager = new ProcessManager(config, validator);
        healthMonitor = new HealthMonitor(config, processManager);
    }

    private void runLauncher() {
        try {
            log.info("Starting Lampa Launcher...");

            // Валидация окружения
            validator.validateEnvironment();

            // Запуск TorrServer
            processManager.startTorrServer();

            // Запуск Lampa
            processManager.startLampa();

            log.info("All processes started successfully");

            // Ожидание завершения Lampa
            processManager.waitForLampaExit();

        } catch (Exception e) {
            log.error("Launcher execution failed", e);
        }
    }
}
