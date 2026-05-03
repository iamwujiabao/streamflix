package com.streamflix.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.streamflix.client.model.*;

import java.util.List;
import java.util.Map;

/**
 * Strongly-typed convenience wrappers around {@link ApiClient}.
 */
public class StreamFlixApi {

    private final ApiClient client;

    public StreamFlixApi(ApiClient client) { this.client = client; }

    public ApiClient client() { return client; }

    // ---------------- Auth ----------------
    public AuthResponse register(String username, String email, String password,
                                   String fullName, String country) {
        AuthResponse r = client.post("/auth/register", Map.of(
                "username", username, "email", email, "password", password,
                "fullName", fullName == null ? "" : fullName,
                "country",  country  == null ? "" : country),
                new TypeReference<>() {});
        client.setToken(r.token);
        return r;
    }

    public AuthResponse login(String username, String password) {
        AuthResponse r = client.post("/auth/login",
                Map.of("username", username, "password", password),
                new TypeReference<>() {});
        client.setToken(r.token);
        return r;
    }

    public User me() {
        return client.get("/auth/me", new TypeReference<>() {});
    }

    public void logout() { client.setToken(null); }

    // ---------------- Videos ----------------
    public PageResponse<Video> listVideos(int page, int size) {
        return client.get("/videos?page=" + page + "&size=" + size, new TypeReference<>() {});
    }

    public PageResponse<Video> searchVideos(String query, int page, int size) {
        return client.get("/videos/search?q=" + encode(query) + "&page=" + page + "&size=" + size,
                new TypeReference<>() {});
    }

    public PageResponse<Video> filterVideos(Map<String, Object> filter, int page, int size) {
        return client.post("/videos/filter?page=" + page + "&size=" + size, filter,
                new TypeReference<>() {});
    }

    public List<Video> trendingVideos(int limit) {
        return client.get("/videos/trending?limit=" + limit, new TypeReference<>() {});
    }

    public List<Video> recommendations(int limit) {
        return client.get("/videos/recommendations?limit=" + limit, new TypeReference<>() {});
    }

    public Video getVideo(long id) {
        return client.get("/videos/" + id, new TypeReference<>() {});
    }

    public void recordWatch(long videoId, int watchDuration, double progressPct, String device) {
        client.post("/videos/" + videoId + "/watch",
                Map.of("watchDuration", watchDuration,
                       "progressPct",   progressPct,
                       "deviceType",    device),
                new TypeReference<>() {});
    }

    public String like(long videoId) {
        return client.postEmpty("/videos/" + videoId + "/like", new TypeReference<>() {});
    }

    public String dislike(long videoId) {
        return client.postEmpty("/videos/" + videoId + "/dislike", new TypeReference<>() {});
    }

    // ---------------- Comments ----------------
    public PageResponse<Comment> commentsForVideo(long videoId, int page, int size) {
        return client.get("/comments/video/" + videoId + "?page=" + page + "&size=" + size,
                new TypeReference<>() {});
    }

    public Comment postComment(long videoId, String content, Long parentId) {
        Map<String, Object> body = parentId == null
                ? Map.of("content", content)
                : Map.of("content", content, "parentCommentId", parentId);
        return client.post("/comments/video/" + videoId, body, new TypeReference<>() {});
    }

    // ---------------- Channels ----------------
    public List<Channel> listChannels() {
        return client.get("/channels", new TypeReference<>() {});
    }

    public String subscribeChannel(long channelId) {
        return client.postEmpty("/channels/" + channelId + "/subscribe", new TypeReference<>() {});
    }

    // ---------------- History ----------------
    public PageResponse<Map<String, Object>> myHistory(int page, int size) {
        return client.get("/users/me/history?page=" + page + "&size=" + size,
                new TypeReference<>() {});
    }

    // ---------------- Playlists ----------------
    public List<Map<String, Object>> myPlaylists() {
        return client.get("/playlists/me", new TypeReference<>() {});
    }

    public Map<String, Object> createPlaylist(String title, String description, boolean isPublic) {
        return client.post("/playlists",
                Map.of("title", title, "description", description == null ? "" : description,
                        "isPublic", isPublic),
                new TypeReference<>() {});
    }

    public List<Video> playlistVideos(long playlistId) {
        return client.get("/playlists/" + playlistId + "/videos", new TypeReference<>() {});
    }

    public void addToPlaylist(long playlistId, long videoId) {
        client.postEmpty("/playlists/" + playlistId + "/videos/" + videoId, new TypeReference<>() {});
    }

    // ---------------- Notifications ----------------
    public PageResponse<Map<String, Object>> notifications(int page, int size) {
        return client.get("/notifications?page=" + page + "&size=" + size,
                new TypeReference<>() {});
    }

    public long unreadCount() {
        Map<String, Object> r = client.get("/notifications/unread-count", new TypeReference<>() {});
        Object n = r.get("count");
        return n instanceof Number ? ((Number) n).longValue() : 0L;
    }

    // ---------------- Subscriptions ----------------
    public List<Map<String, Object>> plans() {
        return client.get("/subscriptions/plans", new TypeReference<>() {});
    }

    public Map<String, Object> subscribe(int planId, int months, String paymentMethod) {
        return client.post("/subscriptions/subscribe",
                Map.of("planId", planId, "months", months, "paymentMethod", paymentMethod),
                new TypeReference<>() {});
    }

    public Map<String, Object> activeSubscription() {
        return client.get("/subscriptions/me/active", new TypeReference<>() {});
    }

    // ---------------- Categories ----------------
    public List<Map<String, Object>> categories() {
        return client.get("/categories", new TypeReference<>() {});
    }

    private static String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
