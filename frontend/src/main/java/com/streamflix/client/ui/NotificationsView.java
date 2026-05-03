package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.model.PageResponse;
import com.streamflix.client.util.AsyncRunner;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;

public class NotificationsView extends BorderPane {

    private final ListView<String> list = new ListView<>();

    public NotificationsView() {
        setPadding(new Insets(20));
        Label h = new Label("🔔 Notifications");
        h.getStyleClass().add("page-title");

        Button readAll = new Button("Mark all as read");
        readAll.setOnAction(e -> AsyncRunner.run(
                () -> { AppContext.get().api().client()
                        .post("/notifications/read-all", Map.of(),
                                new com.fasterxml.jackson.core.type.TypeReference<>() {});
                        return null; },
                r -> reload()));

        HBox header = new HBox(10, h, new Region(), readAll);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        setTop(header);
        setCenter(list);
        reload();
    }

    private void reload() {
        list.getItems().setAll("Loading...");
        AsyncRunner.run(
                () -> AppContext.get().api().notifications(0, 50),
                (PageResponse<Map<String, Object>> page) -> {
                    list.getItems().clear();
                    if (page.content == null || page.content.isEmpty()) {
                        list.getItems().add("(no notifications)");
                        return;
                    }
                    for (Map<String, Object> n : page.content) {
                        String prefix = Boolean.TRUE.equals(n.get("isRead")) ? "  " : "● ";
                        list.getItems().add(prefix + n.get("type") + " — " + n.get("content"));
                    }
                });
    }
}
