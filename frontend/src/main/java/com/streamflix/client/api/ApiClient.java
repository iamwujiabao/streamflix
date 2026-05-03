package com.streamflix.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.streamflix.client.model.ApiEnvelope;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Thin HTTP client over the StreamFlix REST API.
 * Holds the JWT token in memory and adds it to every request.
 * Uses Java 11+ {@link HttpClient}; no external HTTP dependency.
 */
public class ApiClient {

    private final String     baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private String token;       // may be null when anonymous

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void setToken(String token) { this.token = token; }
    public String getToken()             { return token; }
    public boolean isAuthenticated()     { return token != null && !token.isBlank(); }

    public <T> T get(String path, TypeReference<ApiEnvelope<T>> typeRef) throws ApiException {
        return execute(buildRequest(path).GET().build(), typeRef);
    }

    public <T> T post(String path, Object body,
                      TypeReference<ApiEnvelope<T>> typeRef) throws ApiException {
        try {
            String json = body == null ? "{}" : mapper.writeValueAsString(body);
            HttpRequest req = buildRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return execute(req, typeRef);
        } catch (IOException e) {
            throw new ApiException("Failed to encode body", e);
        }
    }

    public <T> T delete(String path, TypeReference<ApiEnvelope<T>> typeRef) throws ApiException {
        return execute(buildRequest(path).DELETE().build(), typeRef);
    }

    /** POST with no body (for like / dislike toggles). */
    public <T> T postEmpty(String path, TypeReference<ApiEnvelope<T>> typeRef) throws ApiException {
        return post(path, Map.of(), typeRef);
    }

    private HttpRequest.Builder buildRequest(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        return b;
    }

    private <T> T execute(HttpRequest request,
                           TypeReference<ApiEnvelope<T>> typeRef) throws ApiException {
        try {
            HttpResponse<String> res = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 401) {
                throw new ApiException("Authentication required (401)");
            }
            ApiEnvelope<T> env = mapper.readValue(res.body(), typeRef);
            if (!env.success) {
                throw new ApiException(env.message != null ? env.message : "Request failed");
            }
            return env.data;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Network error: " + e.getMessage(), e);
        }
    }
}
