package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.exception.LampaLaunchException;
import com.puhovin.lampalauncher.exception.TorrServerLaunchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessManagerTest {

    @Mock
    private TorrServerDaemonProcess torrServer;

    @Mock
    private LampaProcess lampa;

    private ProcessManager processManager;

    @BeforeEach
    void setUp() {
        Config config = new Config(
                Path.of("torrserver.exe"),
                8090,
                30,
                Path.of("lampa.exe")
        );
        processManager = new ProcessManager(config);
        injectMock(processManager, "torrServer", torrServer);
        injectMock(processManager, "lampa", lampa);
    }

    @Test
    void startAll_successfulStart_noExceptionThrown() throws Exception {
        doNothing().when(torrServer).start();
        doNothing().when(torrServer).waitForPort(any(Duration.class));
        doNothing().when(lampa).start();

        assertThatNoException().isThrownBy(() -> processManager.startAll());

        verify(torrServer).start();
        verify(torrServer).waitForPort(any(Duration.class));
        verify(lampa).start();
    }

    @Test
    void startAll_torrServerFailsToStart_throwsTorrServerException() throws Exception {
        doThrow(new IOException("Failed to start")).when(torrServer).start();

        assertThatThrownBy(() -> processManager.startAll())
                .isInstanceOf(TorrServerLaunchException.class)
                .hasMessageContaining("Failed to start TorrServer");
    }

    @Test
    void startAll_lampaFailsToStart_throwsLampaLaunchException() throws Exception {
        doNothing().when(torrServer).start();
        doNothing().when(torrServer).waitForPort(any(Duration.class));
        doThrow(new IOException("Failed to start")).when(lampa).start();

        assertThatThrownBy(() -> processManager.startAll())
                .isInstanceOf(LampaLaunchException.class)
                .hasMessageContaining("Failed to start Lampa");
    }

    @Test
    void shutdown_aliveProcesses_stopsAllProcesses() {
        doReturn(true).when(torrServer).isAlive();
        doReturn(true).when(lampa).isAlive();
        doNothing().when(torrServer).stop();
        doNothing().when(lampa).stop();

        processManager.shutdown();

        verify(torrServer).stop();
        verify(lampa).stop();
    }

    @Test
    void shutdown_noAliveProcesses_doesNothing() {
        doReturn(false).when(torrServer).isAlive();
        doReturn(false).when(lampa).isAlive();

        processManager.shutdown();

        verify(torrServer).isAlive();
        verify(lampa).isAlive();

        verify(torrServer, never()).stop();
        verify(lampa, never()).stop();
    }

    @Test
    void waitForLampaExit_callsLampaWaitForExit() throws InterruptedException {
        doNothing().when(lampa).waitForExit();

        processManager.waitForLampaExit();

        verify(lampa).waitForExit();
    }

    private static void injectMock(Object target, String fieldName, Object value) {
        try {
            Field field = ProcessManager.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to inject mock into field: " + fieldName, e);
        }
    }
}
