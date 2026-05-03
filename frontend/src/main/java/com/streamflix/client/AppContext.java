package com.streamflix.client;

import com.streamflix.client.api.ApiClient;
import com.streamflix.client.api.StreamFlixApi;
import com.streamflix.client.model.User;

/**
 * Application-wide singleton holding the API client and the current user.
 */
public final class AppContext {

    private static AppContext instance;

    private final StreamFlixApi api;
    private User currentUser;

    private AppContext(String baseUrl) {
        this.api = new StreamFlixApi(new ApiClient(baseUrl));
    }

    public static AppContext init(String baseUrl) {
        if (instance == null) instance = new AppContext(baseUrl);
        return instance;
    }

    public static AppContext get() {
        if (instance == null) {
            throw new IllegalStateException("AppContext.init(...) must be called first");
        }
        return instance;
    }

    public StreamFlixApi api()                { return api; }
    public User getCurrentUser()              { return currentUser; }
    public void setCurrentUser(User user)     { this.currentUser = user; }
    public boolean isAuthenticated()          { return currentUser != null; }

    public void logout() {
        api.logout();
        currentUser = null;
    }
}
