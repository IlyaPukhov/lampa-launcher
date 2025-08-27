package com.puhovin.lampalauncher.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NetworkUtilsTest {

    @Test
    void isPortInUse_portFree_returnsFalse() {
        int freePort = findFreePort();

        boolean result = NetworkUtils.isPortInUse(freePort);

        assertThat(result).isFalse();
    }

    @Test
    void isPortInUse_portOccupied_returnsTrue() throws IOException {
        int occupiedPort = findFreePort();

        try (var ignored = new ServerSocket(occupiedPort)) {
            boolean result = NetworkUtils.isPortInUse(occupiedPort);

            assertThat(result).isTrue();
        }
    }

    @Test
    void isPortInUse_invalidPort_returnsFalse() {
        int invalidPort = -1;

        assertThatThrownBy(() -> NetworkUtils.isPortInUse(invalidPort))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("port out of range:" + invalidPort);
    }

    @SneakyThrows
    private int findFreePort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
