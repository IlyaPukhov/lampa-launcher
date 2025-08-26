package com.puhovin.lampalauncher.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigLoader {

    private ConfigLoader() {}

    public static Config load(String path) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        }
        return new Config(
                Path.of(props.getProperty("torrserver.path")),
                Integer.parseInt(props.getProperty("torrserver.port")),
                Path.of(props.getProperty("lampa.path"))
        );
    }
}

