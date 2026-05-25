package com.auction.client.network.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Resolves the auction server endpoint without coupling the client to one machine.
 */
public final class ClientNetworkConfig {
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_PORT = 8080;

    private static final String HOST_ENV = "AUCTION_SERVER_HOST";
    private static final String PORT_ENV = "AUCTION_SERVER_PORT";
    private static final String HOST_PROPERTY = "server.host";
    private static final String PORT_PROPERTY = "server.port";
    private static final Path CONFIG_PATH = Path.of("client.properties");

    private ClientNetworkConfig() {
    }

    public static String getServerHost() {
        return resolve(System.getenv(), CONFIG_PATH).host();
    }

    public static int getServerPort() {
        return resolve(System.getenv(), CONFIG_PATH).port();
    }

    static ConnectionSettings resolve(Map<String, String> environment, Path configPath) {
        ConnectionSettings fileSettings = loadFileSettings(configPath);

        String host = nonBlank(environment.get(HOST_ENV));
        if (host == null) {
            host = fileSettings.host();
        }

        Integer port = parsePort(environment.get(PORT_ENV));
        if (port == null) {
            port = fileSettings.port();
        }

        return new ConnectionSettings(host, port);
    }

    private static ConnectionSettings loadFileSettings(Path configPath) {
        if (!Files.isRegularFile(configPath)) {
            return defaults();
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
        } catch (IOException exception) {
            return defaults();
        }

        String host = nonBlank(properties.getProperty(HOST_PROPERTY));
        Integer port = parsePort(properties.getProperty(PORT_PROPERTY));
        if (host == null || port == null) {
            return defaults();
        }

        return new ConnectionSettings(host, port);
    }

    private static ConnectionSettings defaults() {
        return new ConnectionSettings(DEFAULT_HOST, DEFAULT_PORT);
    }

    private static String nonBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static Integer parsePort(String value) {
        String normalizedValue = nonBlank(value);
        if (normalizedValue == null) {
            return null;
        }

        try {
            int port = Integer.parseInt(normalizedValue);
            return port >= 1 && port <= 65535 ? port : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    record ConnectionSettings(String host, int port) {
    }
}
