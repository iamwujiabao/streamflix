package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.util.AsyncRunner;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;

public class PlaylistsView extends BorderPane {

    private final ListView<String> list = new ListView<>();

    public PlaylistsView() {
        setPadding(new Insets(20));
        Label h = new Label("📋 My Playlists");
        h.getStyleClass().add("page-title");

        TextField title = new TextField(); title.setPromptText("New playlist title");
        Button create = new Button("Create");
        create.getStyleClass().add("primary");
        create.setOnAction(e -> {
            if (title.getText().isBlank()) return;
            AsyncRunner.run(
                    () -> AppContext.get().api().createPlaylist(title.getText().trim(), "", true),
                    r -> { title.clear(); reload(); });
        });

        HBox createRow = new HBox(8, title, create);
        HBox.setHgrow(title, Priority.ALWAYS);

        VBox top = new VBox(10, h, createRow);
        setTop(top);
        setCenter(list);

        reload();
    }

    private void reload() {
        list.getItems().setAll("Loading...");
        AsyncRunner.run(
                () -> AppContext.get().api().myPlaylists(),
                playlists -> {
                    list.getItems().clear();
                    if (playlists == null || playlists.isEmpty()) {
                        list.getItems().add("(no playlists yet)");
                        return;
                    }
                    for (Map<String, Object> p : playlists) {
                        list.getItems().add(p.get("title") + "  —  " + p.get("description"));
                    }
                });
    }
}
