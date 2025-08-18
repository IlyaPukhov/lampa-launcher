package com.puhovin.lampalauncher;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.Locale.ENGLISH;

public final class LoggerSetup {

    private static final Logger LOGGER = Logger.getLogger(LoggerSetup.class.getName());

    private static final String LOG_FILE_NAME = "launcher.log";
    private static final int MAX_FILE_SIZE_BYTES = 1024 * 1024; // 1 MB
    private static final int MAX_FILE_COUNT = 1;

    private LoggerSetup() {}

    public static void init() throws IOException {
        FileHandler fh = new FileHandler(LOG_FILE_NAME, MAX_FILE_SIZE_BYTES, MAX_FILE_COUNT, true);
        fh.setEncoding("UTF-8");
        fh.setFormatter(new CustomFormatter());

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(fh);

        LOGGER.info("Logging initialized: single file, max " + MAX_FILE_SIZE_BYTES + " bytes.");
    }

    // Custom formatter with fixed date format
    private static class CustomFormatter extends Formatter {
        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(ENGLISH);

        @Override
        public String format(LogRecord record) {
            String timestamp = DATE_FORMAT.format(LocalDateTime.now());
            return String.format("[%s] %s: %s%n", timestamp, record.getLevel(), record.getMessage());
        }
    }
}
