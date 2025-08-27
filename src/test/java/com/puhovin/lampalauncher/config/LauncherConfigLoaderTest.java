package com.puhovin.lampalauncher.config;

import com.puhovin.lampalauncher.exception.LauncherConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LauncherConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void getConfig_validProperties_returnsConfigWithValues() throws IOException, LauncherConfigurationException {
        Path configFile = tempDir.resolve("launcher.properties");
        Properties props = new Properties();
        props.setProperty("torrserver.path", "/path/torrserver.exe");
        props.setProperty("torrserver.port", "8090");
        props.setProperty("lampa.path", "/path/lampa.exe");
        props.setProperty("torrserver.startup-timeout", "45");
        try (var output = Files.newOutputStream(configFile)) {
            props.store(output, null);
        }

        LauncherConfigLoader loader = new LauncherConfigLoader(configFile);
        Config config = loader.getConfig();

        assertThat(config.torrServerPath()).isEqualTo(Path.of("/path/torrserver.exe"));
        assertThat(config.torrServerPort()).isEqualTo(8090);
        assertThat(config.lampaPath()).isEqualTo(Path.of("/path/lampa.exe"));
        assertThat(config.torrServerStartupTimeout()).isEqualTo(45);
    }

    @Test
    void constructor_missingPropertiesFile_throwsException() {
        Path missingFile = tempDir.resolve("missing.properties");

        assertThatThrownBy(() -> new LauncherConfigLoader(missingFile))
                .isInstanceOf(LauncherConfigurationException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getConfig_invalidPortInProperties_usesDefault() throws IOException, LauncherConfigurationException {
        Path configFile = tempDir.resolve("launcher.properties");
        Properties props = new Properties();
        props.setProperty("torrserver.port", "invalid");
        try (var output = Files.newOutputStream(configFile)) {
            props.store(output, null);
        }

        LauncherConfigLoader loader = new LauncherConfigLoader(configFile);
        Config config = loader.getConfig();

        assertThat(config.torrServerPort()).isEqualTo(8090);
    }

    @Test
    void getConfig_missingProperties_usesDefaults() throws IOException, LauncherConfigurationException {
        Path configFile = tempDir.resolve("launcher.properties");
        try (var output = Files.newOutputStream(configFile)) {
            new Properties().store(output, null);
        }

        LauncherConfigLoader loader = new LauncherConfigLoader(configFile);
        Config config = loader.getConfig();

        assertThat(config.torrServerPath()).isEqualTo(Path.of("./torrserver/torrserver.exe"));
        assertThat(config.torrServerPort()).isEqualTo(8090);
        assertThat(config.lampaPath()).isEqualTo(Path.of("./lampa/lampa.exe"));
        assertThat(config.torrServerStartupTimeout()).isEqualTo(30);
    }
}
