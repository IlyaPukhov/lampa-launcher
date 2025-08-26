package com.puhovin.lampalauncher.config;

import java.nio.file.Path;

/**
 * Holds configuration values loaded from properties file.
 */
public record Config(Path torrPath, int torrPort, Path lampaPath) {
}
