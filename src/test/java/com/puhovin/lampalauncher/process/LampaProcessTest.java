package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LampaProcessTest {

    @Test
    void isAlive_processNotStarted_returnsFalse() {
        Path path = Path.of("lampa.exe");
        LampaProcess lampaProcess = new LampaProcess(getConfig(path));

        boolean alive = lampaProcess.isAlive();

        assertThat(alive).isFalse();
    }

    @Test
    void stop_processNotStarted_doesNothing() {
        Path path = Path.of("lampa.exe");
        LampaProcess lampaProcess = new LampaProcess(getConfig(path));

        assertThatNoException().isThrownBy(lampaProcess::stop);
    }

    @Test
    void waitForExit_processNotStarted_doesNothing() {
        Path path = Path.of("lampa.exe");
        LampaProcess lampaProcess = new LampaProcess(getConfig(path));

        assertThatNoException().isThrownBy(lampaProcess::waitForExit);
    }

    @Test
    void start_validProcess_startsProcess() throws Exception {
        Path path = Path.of("lampa.exe");

        try (var mocked = mockConstruction(ProcessBuilder.class, (builderMock, context) -> {
            Process mockProcess = mock(Process.class);

            doReturn(mockProcess).when(builderMock).start();
            doReturn(true).when(mockProcess).isAlive();
        })) {

            LampaProcess lampaProcess = new LampaProcess(getConfig(path));

            assertThatNoException().isThrownBy(lampaProcess::start);

            assertThat(lampaProcess.isAlive()).isTrue();

            assertThat(mocked.constructed()).hasSize(1);
            verify(mocked.constructed().getFirst()).start();
        }
    }

    @Test
    void stop_processRunning_stopsProcess() throws Exception {
        Path path = Path.of("lampa.exe");
        AtomicReference<Process> processRef = new AtomicReference<>();

        try (var ignored = mockConstruction(ProcessBuilder.class, (builderMock, context) -> {
            Process mockProcess = mock(Process.class);
            processRef.set(mockProcess);

            doReturn(mockProcess).when(builderMock).start();
            doReturn(true).when(mockProcess).isAlive();
        })) {

            LampaProcess lampaProcess = new LampaProcess(getConfig(path));
            lampaProcess.start();

            assertThatNoException().isThrownBy(lampaProcess::stop);
            verify(processRef.get(), times(1)).destroy();
        }
    }

    @Test
    void waitForExit_processRunning_waitsForProcess() throws Exception {
        Path path = Path.of("lampa.exe");
        AtomicReference<Process> processRef = new AtomicReference<>();

        try (var ignored = mockConstruction(ProcessBuilder.class, (builderMock, context) -> {
            Process mockProcess = mock(Process.class);
            processRef.set(mockProcess);

            doReturn(mockProcess).when(builderMock).start();
        })) {

            LampaProcess lampaProcess = new LampaProcess(getConfig(path));
            lampaProcess.start();

            assertThatNoException().isThrownBy(lampaProcess::waitForExit);
            verify(processRef.get(), times(1)).waitFor();
        }
    }

    @Test
    void start_processThrowsIOException_throwsException() {
        Path path = Path.of("lampa.exe");

        try (var ignored = mockConstruction(ProcessBuilder.class, (builderMock, context) ->
                doThrow(new IOException("Cannot start")).when(builderMock).start()
        )) {

            LampaProcess lampaProcess = new LampaProcess(getConfig(path));

            assertThatThrownBy(lampaProcess::start)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Cannot start");
        }
    }

    private Config getConfig(Path path) {
        return new Config(path, 8090, 30, path);
    }
}
