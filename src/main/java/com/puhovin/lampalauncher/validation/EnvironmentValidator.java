package com.puhovin.lampalauncher.validation;

import com.puhovin.lampalauncher.config.LauncherConfiguration;
import com.puhovin.lampalauncher.exception.EnvironmentValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public record EnvironmentValidator(LauncherConfiguration config) {

    /**
     * Выполняет полную валидацию окружения
     */
    public void validateEnvironment() throws EnvironmentValidationException {
        log.info("Validating environment...");

        validateExecutables();
        validatePorts();
        validatePermissions();

        log.info("Environment validation completed successfully");
    }

    /**
     * Проверяет наличие исполняемых файлов
     */
    private void validateExecutables() throws EnvironmentValidationException {
        Path torrServerPath = config.getTorrServerPath();
        if (!Files.exists(torrServerPath)) {
            throw new EnvironmentValidationException(
                    "TorrServer executable not found: " + torrServerPath);
        }

        if (!Files.isExecutable(torrServerPath)) {
            throw new EnvironmentValidationException(
                    "TorrServer file is not executable: " + torrServerPath);
        }

        Path lampaPath = config.getLampaPath();
        if (!Files.exists(lampaPath)) {
            throw new EnvironmentValidationException(
                    "Lampa executable not found: " + lampaPath);
        }

        if (!Files.isExecutable(lampaPath)) {
            throw new EnvironmentValidationException(
                    "Lampa file is not executable: " + lampaPath);
        }

        log.debug("Executables validation passed");
    }

    /**
     * Проверяет доступность портов
     */
    private void validatePorts() throws EnvironmentValidationException {
        int port = config.getTorrServerPort();

        if (isPortInUse(port)) {
            throw new EnvironmentValidationException(
                    "Port already in use: " + port);
        }

        log.debug("Port {} is available", port);
    }

    /**
     * Проверяет права доступа
     */
    private void validatePermissions() throws EnvironmentValidationException {
        // Проверяем права на запись в текущую директорию (для логов)
        Path currentDir = Path.of(".");
        if (!Files.isWritable(currentDir)) {
            throw new EnvironmentValidationException(
                    "No write permissions in current directory");
        }

        log.debug("Permissions validation passed");
    }

    /**
     * Проверяет, используется ли порт
     */
    public boolean isPortInUse(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Проверяет доступность TorrServer по HTTP
     */
    public boolean isTorrServerResponding(int port) {
        return isPortInUse(port); // Упрощенная проверка
    }
}
