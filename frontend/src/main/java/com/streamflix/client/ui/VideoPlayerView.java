package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.model.Comment;
import com.streamflix.client.model.PageResponse;
import com.streamflix.client.model.Video;
import com.streamflix.client.util.AsyncRunner;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Modal "video page": shows metadata, like/dislike/save buttons, and
 * the comment thread.
 */
public class VideoPlayerView {

    private final Video video;
    private final VBox  commentList = new VBox(8);

    public VideoPlayerView(Video video) { this.video = video; }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(video.title);

        // ---- Player placeholder ----
        StackPane player = new StackPane();
        player.setPrefSize(720, 405);
        player.setBackground(new Background(new BackgroundFill(
                Color.BLACK, new CornerRadii(6), Insets.EMPTY)));
        Label playGlyph = new Label("▶");
        playGlyph.setStyle("-fx-text-fill: #555; -fx-font-size: 64;");
        player.getChildren().add(playGlyph);

        // ---- Title & meta ----
        Label title = new Label(video.title);
        title.setFont(Font.font(null, FontWeight.BOLD, 20));
        Label channelLabel = new Label("📺 " + (video.channelName == null ? "—" : video.channelName));
        Label statsLabel = new Label(
                (video.viewsCount == null ? 0 : video.viewsCount) + " views • 👍 "
                + (video.likesCount == null ? 0 : video.likesCount) + " • 👎 "
                + (video.dislikesCount == null ? 0 : video.dislikesCount));
        statsLabel.getStyleClass().add("muted");

        // ---- Action buttons ----
        Button likeBtn    = new Button("👍 Like");
        Button dislikeBtn = new Button("👎 Dislike");
        Button watchBtn   = new Button("✓ Mark watched");

        likeBtn.setOnAction(e -> AsyncRunner.run(
                () -> AppContext.get().api().like(video.videoId),
                r -> AsyncRunner.showInfo("Reaction: " + r)));
        dislikeBtn.setOnAction(e -> AsyncRunner.run(
                () -> AppContext.get().api().dislike(video.videoId),
                r -> AsyncRunner.showInfo("Reaction: " + r)));
        watchBtn.setOnAction(e -> AsyncRunner.run(
                () -> { AppContext.get().api().recordWatch(video.videoId,
                        video.durationSec == null ? 60 : video.durationSec, 100.0, "DESKTOP");
                        return null; },
                r -> AsyncRunner.showInfo("Watch recorded — view counter incremented by trigger")));

        HBox actions = new HBox(8, likeBtn, dislikeBtn, watchBtn);

        Label desc = new Label(video.description == null ? "(No description)" : video.description);
        desc.setWrapText(true);

        // ---- Comments section ----
        Label commentsHeader = new Label("Comments");
        commentsHeader.setFont(Font.font(null, FontWeight.BOLD, 14));

        TextField commentInput = new TextField();
        commentInput.setPromptText("Add a comment...");
        Button postBtn = new Button("Post");
        postBtn.getStyleClass().add("primary");
        postBtn.setOnAction(e -> {
            if (commentInput.getText().isBlank()) return;
            String content = commentInput.getText().trim();
            postBtn.setDisable(true);
            AsyncRunner.run(
                    () -> AppContext.get().api().postComment(video.videoId, content, null),
                    c -> {
                        commentInput.clear();
                        postBtn.setDisable(false);
                        loadComments();
                    });
        });
        HBox commentBox = new HBox(8, commentInput, postBtn);
        HBox.setHgrow(commentInput, Priority.ALWAYS);

        loadComments();

        VBox root = new VBox(12,
                player, title, channelLabel, statsLabel, actions, desc,
                new Separator(),
                commentsHeader, commentBox,
                new ScrollPane(commentList) {{ setFitToWidth(true); setPrefHeight(220); }});
        root.setPadding(new Insets(20));
        root.setMinWidth(760);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/streamflix.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void loadComments() {
        commentList.getChildren().setAll(new Label("Loading..."));
        AsyncRunner.run(
                () -> AppContext.get().api().commentsForVideo(video.videoId, 0, 50),
                (PageResponse<Comment> page) -> {
                    commentList.getChildren().clear();
                    if (page.content == null || page.content.isEmpty()) {
                        commentList.getChildren().add(new Label("No comments yet."));
                        return;
                    }
                    for (Comment c : page.content) {
                        commentList.getChildren().add(renderComment(c));
                    }
                });
    }

    private VBox renderComment(Comment c) {
        Label head = new Label(c.username + " • " + c.createdAt);
        head.getStyleClass().add("muted");
        Label body = new Label(c.content);
        body.setWrapText(true);
        VBox box = new VBox(4, head, body);
        box.setPadding(new Insets(6, 0, 6, 0));
        return box;
    }
}
