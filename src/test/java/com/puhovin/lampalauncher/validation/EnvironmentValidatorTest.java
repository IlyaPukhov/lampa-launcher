package com.puhovin.lampalauncher.validation;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.exception.EnvironmentValidationException;
import com.puhovin.lampalauncher.utils.NetworkUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class EnvironmentValidatorTest {

    @TempDir
    Path tempDir;

    @Test
    void validateEnvironment_validEnvironment_noExceptionThrown() throws Exception {
        Path torrServer = Files.createFile(tempDir.resolve("torrserver.exe"));
        Path lampa = Files.createFile(tempDir.resolve("lampa.exe"));
        Config config = new Config(torrServer, 8090, 30, lampa);

        try (var networkUtils = mockStatic(NetworkUtils.class)) {
            networkUtils.when(() -> NetworkUtils.isPortInUse(anyInt())).thenReturn(false);

            EnvironmentValidator validator = new EnvironmentValidator(config);

            assertThatNoException().isThrownBy(validator::validateEnvironment);
        }
    }

    @Test
    void validateEnvironment_portInUse_throwsException() throws Exception {
        Path torrServer = Files.createFile(tempDir.resolve("torrserver.exe"));
        Path lampa = Files.createFile(tempDir.resolve("lampa.exe"));
        Config config = new Config(torrServer, 8090, 30, lampa);

        try (var networkUtils = mockStatic(NetworkUtils.class)) {
            networkUtils.when(() -> NetworkUtils.isPortInUse(anyInt())).thenReturn(true);

            EnvironmentValidator validator = new EnvironmentValidator(config);

            assertThatThrownBy(validator::validateEnvironment)
                    .isInstanceOf(EnvironmentValidationException.class)
                    .hasMessageContaining("Port already in use");
        }
    }

    @Test
    void validateEnvironment_missingExecutable_throwsException() {
        Path missingFile = tempDir.resolve("missing.exe");
        Config config = new Config(missingFile, 8090, 30, missingFile);
        EnvironmentValidator validator = new EnvironmentValidator(config);

        assertThatThrownBy(validator::validateEnvironment)
                .isInstanceOf(EnvironmentValidationException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void validateEnvironment_fileNotExecutable_throwsExceptionWithCorrectMessage() throws Exception {
        Path notExecutable = Files.createFile(tempDir.resolve("torrserver.txt"));
        Config config = new Config(notExecutable, 8090, 30, notExecutable);
        EnvironmentValidator validator = new EnvironmentValidator(config);

        try (var files = mockStatic(Files.class);
             var networkUtils = mockStatic(NetworkUtils.class)) {
            files.when(() -> Files.isExecutable(any())).thenReturn(false);
            files.when(() -> Files.isWritable(any())).thenReturn(true);
            networkUtils.when(() -> NetworkUtils.isPortInUse(anyInt())).thenReturn(false);

            assertThatThrownBy(validator::validateEnvironment)
                    .isInstanceOf(EnvironmentValidationException.class)
                    .hasMessageContaining("file is not executable");
        }
    }

    @Test
    void validateEnvironment_noWritePermissions_throwsException() throws Exception {
        Path torrServer = Files.createFile(tempDir.resolve("torrserver.exe"));
        Path lampa = Files.createFile(tempDir.resolve("lampa.exe"));
        Config config = new Config(torrServer, 8090, 30, lampa);

        try (var files = mockStatic(Files.class);
             var networkUtils = mockStatic(NetworkUtils.class)) {
            files.when(() -> Files.isWritable(any(Path.class))).thenReturn(false);
            networkUtils.when(() -> NetworkUtils.isPortInUse(anyInt())).thenReturn(false);

            EnvironmentValidator validator = new EnvironmentValidator(config);

            assertThatThrownBy(validator::validateEnvironment)
                    .isInstanceOf(EnvironmentValidationException.class)
                    .hasMessageContaining("No write permissions in current directory");
        }
    }
}
