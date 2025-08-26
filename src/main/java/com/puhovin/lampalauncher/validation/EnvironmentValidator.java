package com.puhovin.lampalauncher.validation;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.exception.EnvironmentValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Validates that the environment has required executables, ports and permissions.
 */
@Slf4j
public record EnvironmentValidator(Config config) {

    /**
     * Performs full environment validation.
     *
     * @throws EnvironmentValidationException when validation fails
     */
    public void validateEnvironment() throws EnvironmentValidationException {
        log.info("Validating environment...");

        validateExecutables();
        validatePorts();
        validatePermissions();

        log.info("Environment validation completed successfully");
    }

    public boolean isPortInUse(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void validateExecutables() throws EnvironmentValidationException {
        Path torrServerPath = config.torrServerPath();
        if (!Files.exists(torrServerPath)) {
            throw new EnvironmentValidationException("TorrServer executable not found: " + torrServerPath);
        }

        if (!Files.isExecutable(torrServerPath)) {
            throw new EnvironmentValidationException("TorrServer file is not executable: " + torrServerPath);
        }

        Path lampaPath = config.lampaPath();
        if (!Files.exists(lampaPath)) {
            throw new EnvironmentValidationException("Lampa executable not found: " + lampaPath);
        }

        if (!Files.isExecutable(lampaPath)) {
            throw new EnvironmentValidationException("Lampa file is not executable: " + lampaPath);
        }

        log.debug("Executables validation passed");
    }

    private void validatePorts() throws EnvironmentValidationException {
        int port = config.torrServerPort();
        if (isPortInUse(port)) {
            throw new EnvironmentValidationException("Port already in use: " + port);
        }

        log.debug("Port {} is available", port);
    }

    private void validatePermissions() throws EnvironmentValidationException {
        Path currentDir = Path.of(".");
        if (!Files.isWritable(currentDir)) {
            throw new EnvironmentValidationException("No write permissions in current directory");
        }

        log.debug("Permissions validation passed");
    }
}
