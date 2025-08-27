package com.puhovin.lampalauncher.process;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class StreamGobbler implements Runnable {

    private final InputStream in;
    private final Logger logger;
    private final String processName;
    private final boolean isError;

    @Override
    public void run() {
        MDC.put("process", processName + (isError ? "-err" : "-out"));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isError) {
                    logger.warn(line);
                } else {
                    logger.info(line);
                }
            }
        } catch (IOException e) {
            logger.debug("StreamGobbler for {} stopped: {}", processName, e.getMessage());
        } finally {
            MDC.remove("process");
        }
    }
}
