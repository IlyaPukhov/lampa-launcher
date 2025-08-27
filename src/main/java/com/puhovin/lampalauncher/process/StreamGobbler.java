package com.puhovin.lampalauncher.process;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads process output and logs lines via SLF4J.
 * Uses MDC to separate logs per process (and stdout/stderr).
 */
public record StreamGobbler(
        InputStream inputStream,
        Logger logger,
        String processName,
        String streamType
) implements Runnable {

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            MDC.put("process", processName);
            MDC.put("stream", streamType);

            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("Error reading process stream", e);
        } finally {
            MDC.clear();
        }
    }
}
