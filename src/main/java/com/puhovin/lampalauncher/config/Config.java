package com.puhovin.lampalauncher.config;

import java.nio.file.Path;

/**
 * Record that holds resolved launcher configuration values.
 *
 * @param torrServerPath           path to TorrServer executable
 * @param torrServerPort           port TorrServer should listen on
 * @param torrServerStartupTimeout seconds to wait for TorrServer to start
 * @param lampaPath                path to Lampa executable
 */
public record Config(
        Path torrServerPath,
        int torrServerPort,
        int torrServerStartupTimeout,
        Path lampaPath
) {}
