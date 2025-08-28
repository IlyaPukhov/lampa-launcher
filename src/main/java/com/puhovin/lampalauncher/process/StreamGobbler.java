package com.puhovin.lampalauncher.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads process output and logs lines via SLF4J.
 */
public record StreamGobbler(
        InputStream inputStream,
        String processName,
        boolean isError
) implements Runnable {

    @Override
    public void run() {
        Logger logger = getProcessLogger(processName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isError) {
                    logger.error(line);
                } else {
                    logger.info(line);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading process stream", e);
        }
    }

    private Logger getProcessLogger(String name) {
        return LoggerFactory.getLogger("process." + name);
    }
}
