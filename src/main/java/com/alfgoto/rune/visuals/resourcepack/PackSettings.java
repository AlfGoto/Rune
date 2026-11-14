package com.alfgoto.rune.visuals.resourcepack;

import java.net.URI;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Resource pack serving options from config.yml.
 * A bad value logs a clear error and falls back to the default.
 *
 * @param port port of the built-in HTTP server
 * @param externalUrl pack URL to send instead of the built-in server, or {@code null} to use it
 */
public record PackSettings(int port, String externalUrl) {

    public static final String PORT = "resource-pack.port";
    public static final String EXTERNAL_URL = "resource-pack.external-url";
    public static final int DEFAULT_PORT = 8123;

    public static PackSettings load(ConfigurationSection config, Logger logger) {
        int port = DEFAULT_PORT;
        Object rawPort = config.get(PORT);
        if (rawPort instanceof Number n
                && n.intValue() == n.doubleValue()
                && n.intValue() >= 1
                && n.intValue() <= 65535) {
            port = n.intValue();
        } else {
            logger.severe("Config '" + PORT + "' must be a whole number from 1 to 65535, got '" + rawPort
                    + "'. Using default " + DEFAULT_PORT + ".");
        }

        String url = null;
        String raw = config.getString(EXTERNAL_URL, "");
        if (raw != null && !raw.isBlank()) {
            if (isHttpUrl(raw.strip())) {
                url = raw.strip();
            } else {
                logger.severe("Config '" + EXTERNAL_URL + "' must be an http or https URL, got '" + raw
                        + "'. Using the built-in server.");
            }
        }
        return new PackSettings(port, url);
    }

    static boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            return ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean usesExternalUrl() {
        return externalUrl != null;
    }
}
