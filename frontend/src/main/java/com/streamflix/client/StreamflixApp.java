package com.streamflix.client;

import com.streamflix.client.ui.LoginView;
import com.streamflix.client.util.AsyncRunner;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * StreamFlix desktop client — JavaFX entry point.
 *
 * <p>Defaults to the local backend at {@code http://localhost:8081/api}. Override
 * with the system property {@code -Dstreamflix.api.url=...}.
 */
public class StreamflixApp extends Application {

    public static final String DEFAULT_API_URL = "http://localhost:8081/api";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String apiUrl = System.getProperty("streamflix.api.url", DEFAULT_API_URL);
        AppContext.init(apiUrl);

        new LoginView(primaryStage).show();

        primaryStage.setOnCloseRequest(e -> AsyncRunner.shutdown());
    }
}
