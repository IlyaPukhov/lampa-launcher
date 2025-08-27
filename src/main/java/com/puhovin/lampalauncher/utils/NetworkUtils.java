package com.puhovin.lampalauncher.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class NetworkUtils {

    private static final String HOST = "127.0.0.1";
    private static final int TIMEOUT_MS = 200;

    private NetworkUtils() {}

    public static boolean isPortInUse(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isPortInUse(int port) {
        return isPortInUse(HOST, port, TIMEOUT_MS);
    }
}
