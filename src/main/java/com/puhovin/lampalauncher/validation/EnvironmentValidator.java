package com.puhovin.lampalauncher.validation;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.exception.EnvironmentValidationException;
import com.puhovin.lampalauncher.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;

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

        validatePermissions();
        validateExecutables();
        validatePortAvailable(config.torrServerPort());

        log.info("Environment validation completed successfully");
    }

    private void validateExecutables() throws EnvironmentValidationException {
        validateExecutable(config.torrServerPath(), "TorrServer");
        validateExecutable(config.lampaPath(), "Lampa");
        log.debug("Executables validation passed");
    }

    private void validateExecutable(Path path, String name) throws EnvironmentValidationException {
        if (Files.notExists(path)) {
            throw new EnvironmentValidationException(name + " executable not found: " + path);
        }
        if (!Files.isExecutable(path)) {
            throw new EnvironmentValidationException(name + " file is not executable: " + path);
        }
    }

    private void validatePortAvailable(int port) throws EnvironmentValidationException {
        if (NetworkUtils.isPortInUse(port)) {
            throw new EnvironmentValidationException("Port already in use: " + port);
        }
        log.debug("Port {} is available", port);
    }

    private void validatePermissions() throws EnvironmentValidationException {
        if (!Files.isWritable(Path.of("."))) {
            throw new EnvironmentValidationException("No write permissions in current directory");
        }
        log.debug("Permissions validation passed");
    }
}
