package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Starts TorrServer as a background daemon and monitors its lifecycle
 */
public class TorrServerDaemon {
    private static final Logger LOGGER = Logger.getLogger(TorrServerDaemon.class.getName());

    private final Config config;
    private Process process;

    public TorrServerDaemon(Config config) {
        this.config = config;
    }

    public void start() throws IOException {
        List<String> command = List.of(
                config.torrPath().toString(),
                "--port",
                String.valueOf(config.torrPort())
        );

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectInput(ProcessBuilder.Redirect.PIPE);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        builder.redirectError(ProcessBuilder.Redirect.PIPE);

        process = builder.start();

        // consume output in background threads to avoid blocking
        consumeStream(process.getInputStream());
        consumeStream(process.getErrorStream());

        LOGGER.info("TorrServer started");
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

    public void waitForPort(Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (isPortOpen(config.torrPort())) {
                LOGGER.info("TorrServer port " + config.torrPort() + " is available");
                return;
            }
            TimeUnit.MILLISECONDS.sleep(500);
        }
        throw new IllegalStateException("TorrServer did not start within " + timeout.toSeconds() + " seconds");
    }

    private boolean isPortOpen(int port) {
        try (Socket ignored = new Socket("127.0.0.1", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
            LOGGER.info("TorrServer stopped");
        }
    }
}
