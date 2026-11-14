package com.alfgoto.rune.visuals.resourcepack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/** Tiny built-in HTTP server that serves the resource pack zip at {@code /rune-pack.zip}. */
public final class PackServer {

    public static final String PATH = "/rune-pack.zip";

    private final HttpServer server;

    private PackServer(HttpServer server) {
        this.server = server;
    }

    /** Starts serving {@code zip} on {@code port} (0 = any free port). */
    public static PackServer start(int port, byte[] zip) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(PATH, exchange -> handle(exchange, zip));
        server.start();
        return new PackServer(server);
    }

    private static void handle(HttpExchange exchange, byte[] zip) throws IOException {
        try (exchange) {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, zip.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(zip);
            }
        }
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
    }
}
