package com.puhovin.lampalauncher.config;

import com.puhovin.lampalauncher.exception.LauncherConfigurationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads launcher.properties from the working directory and provides a {@link Config}.
 *
 * <p>The file 'launcher.properties' is required; missing file causes a {@link LauncherConfigurationException}.
 */
@Slf4j
public class LauncherConfigLoader {

    private static final String CONFIG_FILE = "./launcher.properties";

    private final Properties properties;

    public LauncherConfigLoader() throws LauncherConfigurationException {
        this.properties = loadProperties();
    }

    private Properties loadProperties() throws LauncherConfigurationException {
        Path p = Paths.get(CONFIG_FILE);

        if (Files.notExists(p)) {
            throw new LauncherConfigurationException("Configuration file not found: " + p.toAbsolutePath());
        }

        try (InputStream input = Files.newInputStream(p)) {
            Properties props = new Properties();
            props.load(input);
            log.info("Configuration loaded successfully from {}", p.toAbsolutePath());
            return props;
        } catch (IOException e) {
            throw new LauncherConfigurationException("Failed to load configuration from " + p.toAbsolutePath(), e);
        }
    }

    public Config getConfig() {
        return new Config(
                Paths.get(getProperty("torrserver.path", "./torrserver/torrserver.exe")),
                parseIntProperty("torrserver.port", 8090),
                Paths.get(getProperty("lampa.path", "./lampa/lampa.exe")),
                parseIntProperty("torrserver.startup.timeout", 30)
        );
    }

    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private int parseIntProperty(String key, int defaultValue) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for property '{}' -> '{}', using default {}", key, raw, defaultValue);
            return defaultValue;
        }
    }
}
