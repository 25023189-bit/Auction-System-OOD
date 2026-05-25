package com.auction.client.network.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientNetworkConfigTest {

    @TempDir
    Path tempDirectory;

    @Test
    void readsEndpointFromPropertiesFileWhenEnvironmentIsEmpty() throws IOException {
        Path configPath = writeConfig("server.host=192.168.1.25\nserver.port=9090\n");

        ClientNetworkConfig.ConnectionSettings settings =
                ClientNetworkConfig.resolve(Map.of(), configPath);

        assertEquals("192.168.1.25", settings.host());
        assertEquals(9090, settings.port());
    }

    @Test
    void validEnvironmentValuesOverridePropertiesFile() throws IOException {
        Path configPath = writeConfig("server.host=localhost\nserver.port=8080\n");

        ClientNetworkConfig.ConnectionSettings settings = ClientNetworkConfig.resolve(
                Map.of(
                        "AUCTION_SERVER_HOST", "100.64.0.8",
                        "AUCTION_SERVER_PORT", "8181"
                ),
                configPath
        );

        assertEquals("100.64.0.8", settings.host());
        assertEquals(8181, settings.port());
    }

    @Test
    void invalidFileConfigurationFallsBackToLocalDefault() throws IOException {
        Path configPath = writeConfig("server.host=192.168.1.25\nserver.port=70000\n");

        ClientNetworkConfig.ConnectionSettings settings =
                ClientNetworkConfig.resolve(Map.of(), configPath);

        assertEquals("localhost", settings.host());
        assertEquals(8080, settings.port());
    }

    @Test
    void missingPropertiesFileFallsBackToLocalDefault() {
        ClientNetworkConfig.ConnectionSettings settings =
                ClientNetworkConfig.resolve(Map.of(), tempDirectory.resolve("missing.properties"));

        assertEquals("localhost", settings.host());
        assertEquals(8080, settings.port());
    }

    @Test
    void missingPropertyKeyFallsBackToLocalDefault() throws IOException {
        Path configPath = writeConfig("server.host=192.168.1.25\n");

        ClientNetworkConfig.ConnectionSettings settings =
                ClientNetworkConfig.resolve(Map.of(), configPath);

        assertEquals("localhost", settings.host());
        assertEquals(8080, settings.port());
    }

    @Test
    void invalidEnvironmentPortFallsBackToValidFilePort() throws IOException {
        Path configPath = writeConfig("server.host=192.168.1.25\nserver.port=9090\n");

        ClientNetworkConfig.ConnectionSettings settings = ClientNetworkConfig.resolve(
                Map.of("AUCTION_SERVER_PORT", "not-a-port"),
                configPath
        );

        assertEquals("192.168.1.25", settings.host());
        assertEquals(9090, settings.port());
    }

    private Path writeConfig(String content) throws IOException {
        Path path = tempDirectory.resolve("client.properties");
        Files.writeString(path, content);
        return path;
    }
}
