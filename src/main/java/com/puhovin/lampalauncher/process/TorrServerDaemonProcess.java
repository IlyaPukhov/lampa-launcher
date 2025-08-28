package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.utils.NetworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Manages TorrServer external daemon.
 */
@Slf4j
@RequiredArgsConstructor
public class TorrServerDaemonProcess implements ManagedProcess {

    private final Config config;
    private Process process;

    @Override
    public void start() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                config.torrServerPath().toString(),
                "--port",
                String.valueOf(config.torrServerPort())
        );
        processBuilder.redirectErrorStream(false);

        log.info("Starting TorrServer...");
        process = processBuilder.start();
        log.info("TorrServer started (pid={})", process.pid());

        attachProcessStreamReaders(process, "torrserver");
    }

    /**
     * Waits until the configured port is open or the timeout elapses.
     */
    public void waitForPort(Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        int port = config.torrServerPort();
        while (System.nanoTime() < deadline) {
            if (NetworkUtils.isPortInUse(port)) {
                log.info("TorrServer port {} is available", port);
                return;
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }

        throw new IllegalStateException("TorrServer did not open port " + port + " within " + timeout.toSeconds() + "s");
    }

    @Override
    public void stop() {
        if (process != null && process.isAlive()) {
            log.info("Stopping TorrServer...");
            process.destroy();
            log.info("TorrServer stopped");
        }
    }

    @Override
    public boolean isAlive() {
        return process != null && process.isAlive();
    }
}
