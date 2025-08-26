package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;
import com.puhovin.lampalauncher.utils.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Launches TorrServer as a background process and monitors its port.
 */
@Slf4j
@RequiredArgsConstructor
public class TorrServerDaemon implements ManagedProcess {

    private final Config config;
    private Process process;

    @Override
    public void start() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                config.torrServerPath().toString(),
                "--port",
                String.valueOf(config.torrServerPort())
        );

        builder.redirectInput(ProcessBuilder.Redirect.PIPE);
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        builder.redirectError(ProcessBuilder.Redirect.DISCARD);

        process = builder.start();

        // consume output in background threads to avoid blocking
        consumeStream(process.getInputStream());
        consumeStream(process.getErrorStream());

        log.info("TorrServer started (pid={})", process.pid());
    }

    private void consumeStream(InputStream in) {
        Thread t = new Thread(() -> {
            try {
                in.transferTo(OutputStream.nullOutputStream());
            } catch (IOException ignored) {
                // ignore
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Waits until the configured port is open or the timeout elapses.
     */
    public void waitForPort(Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        int port = config.torrServerPort();
        while (System.nanoTime() < deadline) {
            if (HttpUtils.isPortAvailable("127.0.0.1", port, 500)) {
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
            process.destroy();
            log.info("TorrServer stopped");
        }
    }

    @Override
    public boolean isAlive() {
        return process != null && process.isAlive();
    }
}
