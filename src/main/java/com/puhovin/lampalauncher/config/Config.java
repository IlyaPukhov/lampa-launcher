package com.puhovin.lampalauncher.config;

import java.nio.file.Path;

/**
 * Data transfer object that holds resolved launcher configuration values.
 *
 * <p>This DTO is used across the launcher to avoid coupling to the properties source.
 *
 * @param torrServerPath           path to TorrServer executable
 * @param torrServerPort           port TorrServer should listen on
 * @param lampaPath                path to Lampa executable
 * @param torrServerStartupTimeout seconds to wait for TorrServer to start
 */
public record Config(
        Path torrServerPath,
        int torrServerPort,
        Path lampaPath,
        int torrServerStartupTimeout
) {}
