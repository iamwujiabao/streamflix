package com.streamflix.client.ui;

import com.streamflix.client.model.Video;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Lays out a list of {@link VideoCard}s in a wrapping grid.
 */
public class VideoGridView extends BorderPane {

    private final FlowPane grid = new FlowPane(12, 12);
    private final Label    empty = new Label("Nothing to show.");

    public VideoGridView(String title) {
        Label h = new Label(title);
        h.getStyleClass().add("page-title");
        setPadding(new Insets(20));
        setTop(h);

        grid.setPadding(new Insets(16, 0, 0, 0));
        empty.getStyleClass().add("empty");

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");
        setCenter(scroll);
    }

    public void setVideos(List<Video> videos) {
        grid.getChildren().clear();
        if (videos == null || videos.isEmpty()) {
            grid.getChildren().add(empty);
            return;
        }
        for (Video v : videos) {
            grid.getChildren().add(new VideoCard(v, () -> new VideoPlayerView(v).show()));
        }
    }

    public void setLoading() {
        grid.getChildren().setAll(new Label("Loading..."));
    }
}
