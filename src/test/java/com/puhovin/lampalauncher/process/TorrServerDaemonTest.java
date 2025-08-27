package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.utils.NetworkUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TorrServerDaemonTest {

    private final Config config = new Config(
            Path.of("torrserver.exe"),
            8090,
            30,
            Path.of("lampa.exe")
    );

    @Test
    void waitForPort_portBecomesAvailable_noExceptionThrown() {
        TorrServerDaemon daemon = new TorrServerDaemon(config);

        try (var networkUtils = mockStatic(NetworkUtils.class)) {
            networkUtils.when(() -> NetworkUtils.isPortInUse(anyInt()))
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(true);

            assertThatNoException().isThrownBy(() ->
                    daemon.waitForPort(Duration.ofSeconds(5))
            );
        }
    }

    @Test
    void waitForPort_portNeverAvailable_throwsException() {
        TorrServerDaemon daemon = new TorrServerDaemon(config);

        try (var networkUtils = mockStatic(NetworkUtils.class)) {
            networkUtils.when(() -> NetworkUtils.isPortInUse(anyInt())).thenReturn(false);

            Duration timeout = Duration.ofMillis(100);
            assertThatThrownBy(() -> daemon.waitForPort(timeout))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("did not open port");
        }
    }

    @Test
    void start_validProcess_startsProcess() throws Exception {
        try (var mocked = mockConstruction(ProcessBuilder.class, (builderMock, context) -> {
            Process mockProcess = mock(Process.class);
            doReturn(mockProcess).when(builderMock).start();
            doReturn(InputStream.nullInputStream()).when(mockProcess).getInputStream();
            doReturn(InputStream.nullInputStream()).when(mockProcess).getErrorStream();
        })) {

            TorrServerDaemon daemon = new TorrServerDaemon(config);

            assertThatNoException().isThrownBy(daemon::start);
            assertThat(mocked.constructed()).hasSize(1);
            verify(mocked.constructed().getFirst()).start();
        }
    }

    @Test
    void stop_processStarted_terminatesProcess() throws Exception {
        AtomicReference<Process> processRef = new AtomicReference<>();

        try (var ignored = mockConstruction(ProcessBuilder.class, (builderMock, context) -> {
            Process mockProcess = mock(Process.class);
            processRef.set(mockProcess);

            doReturn(mockProcess).when(builderMock).start();
            doReturn(InputStream.nullInputStream()).when(mockProcess).getInputStream();
            doReturn(InputStream.nullInputStream()).when(mockProcess).getErrorStream();
            doReturn(true).when(mockProcess).isAlive();
        })) {

            TorrServerDaemon daemon = new TorrServerDaemon(config);
            daemon.start();

            assertThatNoException().isThrownBy(daemon::stop);
            verify(processRef.get(), times(1)).destroy();
        }
    }

    @Test
    void isAlive_processNotStarted_returnsFalse() {
        TorrServerDaemon daemon = new TorrServerDaemon(config);

        boolean result = daemon.isAlive();

        assertThat(result).isFalse();
    }

    @Test
    void start_processThrowsIOException_throwsException() {
        try (var ignored = mockConstruction(ProcessBuilder.class, (builderMock, context) ->
                doThrow(new IOException("Cannot start")).when(builderMock).start()
        )) {

            TorrServerDaemon daemon = new TorrServerDaemon(config);

            assertThatThrownBy(daemon::start)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Cannot start");
        }
    }
}
