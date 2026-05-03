package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.util.AsyncRunner;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class PlansView extends BorderPane {

    private final FlowPane grid = new FlowPane(12, 12);

    public PlansView() {
        setPadding(new Insets(20));
        Label h = new Label("💎 Subscription Plans");
        h.getStyleClass().add("page-title");
        setTop(h);
        setCenter(new ScrollPane(grid) {{ setFitToWidth(true); }});

        grid.getChildren().add(new Label("Loading..."));
        AsyncRunner.run(
                () -> AppContext.get().api().plans(),
                this::render);
    }

    private void render(List<Map<String, Object>> plans) {
        grid.getChildren().clear();
        if (plans == null || plans.isEmpty()) {
            grid.getChildren().add(new Label("(no plans available)"));
            return;
        }
        for (Map<String, Object> p : plans) {
            VBox card = new VBox(6);
            card.getStyleClass().add("card");
            card.setPadding(new Insets(16));
            card.setMinWidth(220); card.setMaxWidth(220);

            Label name = new Label(String.valueOf(p.get("name")));
            name.getStyleClass().add("card-title");

            Label price = new Label("$" + p.get("price"));
            price.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

            Label specs = new Label(p.get("maxQuality") + " • up to " + p.get("maxDevices") + " devices");
            specs.getStyleClass().add("muted");

            Label desc = new Label(String.valueOf(p.get("description")));
            desc.setWrapText(true);

            ComboBox<Integer> months = new ComboBox<>();
            months.getItems().addAll(1, 3, 6, 12);
            months.setValue(1);
            ComboBox<String> method = new ComboBox<>();
            method.getItems().addAll("CREDIT_CARD", "PAYPAL", "BANK_TRANSFER", "MOMO", "ZALOPAY");
            method.setValue("CREDIT_CARD");

            Button subscribe = new Button("Subscribe");
            subscribe.getStyleClass().add("primary");
            subscribe.setMaxWidth(Double.MAX_VALUE);
            subscribe.setOnAction(e -> AsyncRunner.run(
                    () -> AppContext.get().api().subscribe(
                            ((Number) p.get("planId")).intValue(),
                            months.getValue(),
                            method.getValue()),
                    r -> AsyncRunner.showInfo("Subscribed! Active until " + r.get("endDate"))));

            card.getChildren().addAll(name, price, specs, desc, months, method, subscribe);
            grid.getChildren().add(card);
        }
    }
}
