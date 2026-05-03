package com.streamflix.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.streamflix.client.api.ApiClient;
import com.streamflix.client.api.ApiException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spin up a tiny HTTP server in front of the {@link ApiClient}.
 * No backend dependency required.
 */
class ApiClientTest {

    private HttpServer server;
    private ApiClient  client;
    private int        port;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port   = server.getAddress().getPort();
        server.start();
        client = new ApiClient("http://localhost:" + port);
    }

    @AfterEach
    void stop() { server.stop(0); }

    @Test
    void get_returns_unwrapped_data_from_envelope() {
        server.createContext("/ping", ex -> {
            String body = "{\"success\":true,\"message\":\"OK\",\"data\":{\"value\":42}}";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.close();
        });

        Map<String, Object> data = client.get("/ping",
                new TypeReference<>() {});
        assertEquals(42, ((Number) data.get("value")).intValue());
    }

    @Test
    void failure_envelope_throws_ApiException_with_message() {
        server.createContext("/fail", ex -> {
            String body = "{\"success\":false,\"message\":\"nope\",\"data\":null}";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(400, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.close();
        });

        ApiException ex = assertThrows(ApiException.class,
                () -> client.get("/fail", new TypeReference<>() {}));
        assertTrue(ex.getMessage().contains("nope"));
    }

    @Test
    void status_401_is_translated_to_clear_message() {
        server.createContext("/auth", ex -> {
            ex.sendResponseHeaders(401, 0);
            ex.close();
        });

        ApiException ex = assertThrows(ApiException.class,
                () -> client.get("/auth", new TypeReference<>() {}));
        assertTrue(ex.getMessage().contains("401"));
    }

    @Test
    void token_is_attached_to_authenticated_requests() {
        client.setToken("mytok");
        final String[] capturedAuth = {null};
        server.createContext("/check", ex -> {
            capturedAuth[0] = ex.getRequestHeaders().getFirst("Authorization");
            String body = "{\"success\":true,\"data\":{}}";
            byte[] b = body.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });

        client.get("/check", new TypeReference<>() {});
        assertEquals("Bearer mytok", capturedAuth[0]);
    }
}
