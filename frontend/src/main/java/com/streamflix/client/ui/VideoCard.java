package com.streamflix.client.ui;

import com.streamflix.client.model.Video;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * One tile in the video grid.
 */
public class VideoCard extends VBox {

    public VideoCard(Video v, Runnable onOpen) {
        setSpacing(6);
        setPadding(new Insets(10));
        setMinWidth(240);
        setMaxWidth(240);
        getStyleClass().add("card");

        // Thumbnail placeholder (real impl would load v.thumbnailUrl asynchronously)
        StackPane thumb = new StackPane();
        thumb.setPrefSize(220, 124);
        thumb.setBackground(new Background(new BackgroundFill(
                Color.web("#222"), new CornerRadii(6), Insets.EMPTY)));
        Label glyph = new Label("🎬");
        glyph.setStyle("-fx-font-size: 32; -fx-text-fill: #555;");
        Label dur = new Label(v.formattedDuration());
        dur.getStyleClass().add("duration-pill");
        StackPane.setAlignment(dur, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(dur, new Insets(0, 6, 6, 0));
        thumb.getChildren().addAll(glyph, dur);

        Label title = new Label(v.title);
        title.setWrapText(true);
        title.getStyleClass().add("card-title");

        Label meta = new Label((v.channelName == null ? "—" : v.channelName)
                + " • " + (v.viewsCount == null ? 0 : v.viewsCount) + " views");
        meta.getStyleClass().add("card-meta");

        getChildren().addAll(thumb, title, meta);

        setOnMouseClicked(e -> { if (onOpen != null) onOpen.run(); });
    }
}
