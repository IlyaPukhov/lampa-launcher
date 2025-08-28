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
        TorrServerDaemonProcess torrServerDaemon = new TorrServerDaemonProcess(config);

        try (var networkUtils = mockStatic(NetworkUtils.class)) {
            networkUtils.when(() -> NetworkUtils.isPortInUse(anyInt()))
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(true);

            assertThatNoException().isThrownBy(() ->
                    torrServerDaemon.waitForPort(Duration.ofSeconds(5))
            );
        }
    }

    @Test
    void waitForPort_portNeverAvailable_throwsException() {
        TorrServerDaemonProcess torrServerDaemon = new TorrServerDaemonProcess(config);

        try (var networkUtils = mockStatic(NetworkUtils.class)) {
            networkUtils.when(() -> NetworkUtils.isPortInUse(anyInt())).thenReturn(false);

            Duration timeout = Duration.ofMillis(100);
            assertThatThrownBy(() -> torrServerDaemon.waitForPort(timeout))
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

            TorrServerDaemonProcess torrServerDaemon = new TorrServerDaemonProcess(config);

            assertThatNoException().isThrownBy(torrServerDaemon::start);
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

            TorrServerDaemonProcess torrServerDaemon = new TorrServerDaemonProcess(config);
            torrServerDaemon.start();

            assertThatNoException().isThrownBy(torrServerDaemon::stop);
            verify(processRef.get(), times(1)).destroy();
        }
    }

    @Test
    void isAlive_processNotStarted_returnsFalse() {
        TorrServerDaemonProcess torrServerDaemon = new TorrServerDaemonProcess(config);

        boolean result = torrServerDaemon.isAlive();

        assertThat(result).isFalse();
    }

    @Test
    void start_processThrowsIOException_throwsException() {
        try (var ignored = mockConstruction(ProcessBuilder.class, (builderMock, context) ->
                doThrow(new IOException("Cannot start")).when(builderMock).start()
        )) {

            TorrServerDaemonProcess torrServerDaemon = new TorrServerDaemonProcess(config);

            assertThatThrownBy(torrServerDaemon::start)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Cannot start");
        }
    }
}
