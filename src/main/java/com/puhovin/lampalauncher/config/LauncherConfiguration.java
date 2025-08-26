package com.puhovin.lampalauncher.config;

import com.puhovin.lampalauncher.exception.LauncherConfigurationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public class LauncherConfiguration {

    private static final String CONFIG_FILE = "./launcher.properties";

    private final Properties properties;

    public LauncherConfiguration() throws LauncherConfigurationException {
        this.properties = loadProperties();
    }

    private Properties loadProperties() throws LauncherConfigurationException {
        Properties props = new Properties();

        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new LauncherConfigurationException("Configuration file not found: " + CONFIG_FILE);
            }

            props.load(input);
            log.info("Configuration loaded successfully");
            return props;

        } catch (IOException e) {
            throw new LauncherConfigurationException("Failed to load configuration", e);
        }
    }

    public Path getTorrServerPath() {
        return Paths.get(getProperty("torrserver.executable.path", "./torrserver/torrserver.exe"));
    }

    public int getTorrServerPort() {
        return Integer.parseInt(getProperty("torrserver.port", "8090"));
    }

    public int getTorrServerStartupTimeout() {
        return Integer.parseInt(getProperty("torrserver.startup.timeout.seconds", "30"));
    }

    public Path getLampaPath() {
        return Paths.get(getProperty("lampa.executable.path", "./lampa/lampa.exe"));
    }

    public int getLampaStartupTimeout() {
        return Integer.parseInt(getProperty("lampa.startup.timeout.seconds", "10"));
    }

    public int getShutdownTimeout() {
        return Integer.parseInt(getProperty("shutdown.timeout.seconds", "30"));
    }

    public String getLoggingLevel() {
        return getProperty("logging.level", "INFO");
    }

    public int getHealthCheckInterval() {
        return Integer.parseInt(getProperty("health.check.interval.seconds", "10"));
    }

    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    public void logConfiguration() {
        log.info("Configuration:");
        log.info("  TorrServer path: {}", getTorrServerPath());
        log.info("  TorrServer port: {}", getTorrServerPort());
        log.info("  Lampa path: {}", getLampaPath());
        log.info("  Shutdown timeout: {}s", getShutdownTimeout());
        log.info("  Health check interval: {}s", getHealthCheckInterval());
    }
}
