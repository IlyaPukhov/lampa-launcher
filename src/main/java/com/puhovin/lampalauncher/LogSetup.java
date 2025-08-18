package com.puhovin.lampalauncher;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LogSetup {

    private LogSetup() {}

    public static void init() throws IOException {
        FileHandler fh = new FileHandler("launcher.log", true);
        fh.setEncoding("UTF-8");
        fh.setFormatter(new SimpleFormatter());
        Logger.getLogger("").addHandler(fh);
    }
}

