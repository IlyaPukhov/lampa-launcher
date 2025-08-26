package com.puhovin.lampalauncher.process;

import com.puhovin.lampalauncher.config.LauncherConfiguration;
import com.puhovin.lampalauncher.exception.LampaLaunchException;
import com.puhovin.lampalauncher.exception.LauncherException;
import com.puhovin.lampalauncher.exception.TorrServerException;
import com.puhovin.lampalauncher.validation.EnvironmentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Управление процессами TorrServer и Lampa
 */
@Slf4j
@RequiredArgsConstructor
public class ProcessManager {

    private final LauncherConfiguration config;
    private final EnvironmentValidator validator;

    private Process torrServerProcess;
    private Process lampaProcess;

    /**
     * Запускает TorrServer как демон
     */
    public void startTorrServer() throws TorrServerException {
        log.info("Starting TorrServer...");

        try {
            Path torrServerPath = config.getTorrServerPath();
            ProcessBuilder builder = new ProcessBuilder(torrServerPath.toString());
            builder.directory(torrServerPath.getParent().toFile());

            // Перенаправляем вывод для логирования
            builder.redirectErrorStream(true);

            torrServerProcess = builder.start();

            // Запускаем асинхронное логирование вывода
            startOutputLogging(torrServerProcess, "TorrServer");

            // Ждем запуска с таймаутом
            waitForTorrServerStartup();

            log.info("TorrServer started successfully on port {}", config.getTorrServerPort());

        } catch (IOException e) {
            throw new TorrServerException("Failed to start TorrServer process", e);
        }
    }

    /**
     * Запускает Lampa
     */
    public void startLampa() throws LampaLaunchException {
        log.info("Starting Lampa...");

        try {
            Path lampaPath = config.getLampaPath();
            ProcessBuilder builder = new ProcessBuilder(lampaPath.toString());
            builder.directory(lampaPath.getParent().toFile());

            lampaProcess = builder.start();

            // Запускаем асинхронное логирование вывода
            startOutputLogging(lampaProcess, "Lampa");

            log.info("Lampa started successfully");

        } catch (IOException e) {
            throw new LampaLaunchException("Failed to start Lampa process", e);
        }
    }

    /**
     * Ждет завершения процесса Lampa
     */
    public void waitForLampaExit() throws InterruptedException {
        if (lampaProcess != null) {
            int exitCode = lampaProcess.waitFor();
            log.info("Lampa process exited with code: {}", exitCode);
        }
    }

    /**
     * Корректное завершение всех процессов
     */
    public void shutdown() throws LauncherException {
        log.info("Initiating graceful shutdown...");

        int timeoutSeconds = config.getShutdownTimeout();

        try {
            // Завершаем процессы параллельно
            CompletableFuture<Void> lampaShutdown = CompletableFuture.runAsync(() -> {
                shutdownProcess(lampaProcess, "Lampa");
            });

            CompletableFuture<Void> torrServerShutdown = CompletableFuture.runAsync(() -> {
                shutdownProcess(torrServerProcess, "TorrServer");
            });

            // Ждем завершения с таймаутом
            CompletableFuture.allOf(lampaShutdown, torrServerShutdown)
                    .get(timeoutSeconds, TimeUnit.SECONDS);

            log.info("Graceful shutdown completed");

        } catch (TimeoutException e) {
            log.warn("Graceful shutdown timed out, forcing termination");
            forceKillProcesses();
        } catch (Exception e) {
            throw new LauncherException("Shutdown failed", e);
        }
    }

    /**
     * Проверяет, живы ли процессы
     */
    public boolean areProcessesAlive() {
        boolean torrServerAlive = isProcessAlive(torrServerProcess);
        boolean lampaAlive = isProcessAlive(lampaProcess);

        log.debug("Process status - TorrServer: {}, Lampa: {}",
                torrServerAlive ? "alive" : "dead",
                lampaAlive ? "alive" : "dead");

        return torrServerAlive && lampaAlive;
    }

    private void waitForTorrServerStartup() throws TorrServerException {
        int timeoutSeconds = config.getTorrServerStartupTimeout();
        int port = config.getTorrServerPort();

        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (validator.isTorrServerResponding(port)) {
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TorrServerException("Interrupted while waiting for TorrServer startup");
            }
        }

        throw new TorrServerException(
                "TorrServer failed to start within " + timeoutSeconds + " seconds");
    }

    private void startOutputLogging(Process process, String processName) {
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[{}] {}", processName, line);
                }
            } catch (IOException e) {
                log.warn("Error reading output from {}: {}", processName, e.getMessage());
            }
        });
    }

    private void shutdownProcess(Process process, String processName) {
        if (process == null) {
            return;
        }

        log.info("Shutting down {}...", processName);

        // Пытаемся корректное завершение
        process.destroy();

        try {
            boolean exited = process.waitFor(10, TimeUnit.SECONDS);
            if (exited) {
                log.info("{} shutdown completed", processName);
            } else {
                log.warn("{} did not shutdown gracefully, force killing", processName);
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for {} shutdown", processName);
            process.destroyForcibly();
        }
    }

    private void forceKillProcesses() {
        if (lampaProcess != null) {
            lampaProcess.destroyForcibly();
            log.warn("Forcibly killed Lampa process");
        }

        if (torrServerProcess != null) {
            torrServerProcess.destroyForcibly();
            log.warn("Forcibly killed TorrServer process");
        }
    }

    private boolean isProcessAlive(Process process) {
        return process != null && process.isAlive();
    }
}
