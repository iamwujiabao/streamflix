package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.model.Channel;
import com.streamflix.client.util.AsyncRunner;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ChannelsView extends BorderPane {

    private final FlowPane grid = new FlowPane(12, 12);

    public ChannelsView() {
        setPadding(new Insets(20));
        Label h = new Label("📺 Channels");
        h.getStyleClass().add("page-title");
        setTop(h);

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        setCenter(sp);

        grid.getChildren().add(new Label("Loading..."));
        AsyncRunner.run(
                () -> AppContext.get().api().listChannels(),
                this::render);
    }

    private void render(List<Channel> channels) {
        grid.getChildren().clear();
        if (channels == null || channels.isEmpty()) {
            grid.getChildren().add(new Label("No channels."));
            return;
        }
        for (Channel c : channels) {
            VBox card = new VBox(6);
            card.getStyleClass().add("card");
            card.setPadding(new Insets(14));
            card.setMinWidth(260); card.setMaxWidth(260);

            Label name = new Label(c.name);
            name.getStyleClass().add("card-title");
            Label by = new Label("by " + (c.ownerUsername == null ? "—" : c.ownerUsername));
            by.getStyleClass().add("muted");
            Label subs = new Label((c.subscriberCount == null ? 0 : c.subscriberCount) + " subscribers");
            subs.getStyleClass().add("muted");
            Label desc = new Label(c.description == null ? "" : c.description);
            desc.setWrapText(true);

            Button sub = new Button("Subscribe / Unsubscribe");
            sub.setOnAction(e -> AsyncRunner.run(
                    () -> AppContext.get().api().subscribeChannel(c.channelId),
                    r -> AsyncRunner.showInfo(r)));

            card.getChildren().addAll(name, by, subs, desc, sub);
            grid.getChildren().add(card);
        }
    }
}
