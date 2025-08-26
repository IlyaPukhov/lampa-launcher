package com.puhovin.lampalauncher.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class HttpUtils {

    private HttpUtils() {}

    public static boolean isPortAvailable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
