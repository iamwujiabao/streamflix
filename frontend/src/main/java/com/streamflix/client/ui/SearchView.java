package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.model.PageResponse;
import com.streamflix.client.model.Video;
import com.streamflix.client.util.AsyncRunner;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Advanced filter form. Maps to {@code POST /videos/filter}.
 */
public class SearchView extends BorderPane {

    private final TextField keyword     = new TextField();
    private final ComboBox<String> resolution = new ComboBox<>();
    private final Spinner<Integer> minDur = new Spinner<>(0, 36000, 0, 60);
    private final Spinner<Integer> maxDur = new Spinner<>(0, 36000, 0, 60);
    private final ComboBox<String> sortBy = new ComboBox<>();
    private final ComboBox<String> sortDir = new ComboBox<>();
    private final CheckBox premiumOnly  = new CheckBox("Premium only");

    private final VideoGridView grid = new VideoGridView("");

    public SearchView() {
        setPadding(new Insets(20));
        Label h = new Label("🔍 Search & Filter");
        h.getStyleClass().add("page-title");

        resolution.getItems().addAll("", "SD", "HD", "FHD", "4K");
        resolution.setValue("");

        sortBy.getItems().addAll("upload_date", "views", "likes", "duration", "title");
        sortBy.setValue("upload_date");
        sortDir.getItems().addAll("desc", "asc");
        sortDir.setValue("desc");

        Button apply = new Button("Apply Filters");
        apply.getStyleClass().add("primary");
        apply.setOnAction(e -> runFilter());

        Button reset = new Button("Reset");
        reset.setOnAction(e -> {
            keyword.clear(); resolution.setValue(""); premiumOnly.setSelected(false);
            minDur.getValueFactory().setValue(0); maxDur.getValueFactory().setValue(0);
            sortBy.setValue("upload_date"); sortDir.setValue("desc");
            runFilter();
        });

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8);
        form.setPadding(new Insets(12, 0, 12, 0));
        form.add(new Label("Keyword:"),    0, 0); form.add(keyword,    1, 0, 3, 1);
        form.add(new Label("Resolution:"), 0, 1); form.add(resolution, 1, 1);
        form.add(new Label("Min duration (s):"), 2, 1); form.add(minDur, 3, 1);
        form.add(new Label("Max duration (s):"), 0, 2); form.add(maxDur, 1, 2);
        form.add(premiumOnly, 2, 2, 2, 1);
        form.add(new Label("Sort by:"),    0, 3); form.add(sortBy,    1, 3);
        form.add(new Label("Direction:"),  2, 3); form.add(sortDir,   3, 3);

        HBox actions = new HBox(8, apply, reset);
        VBox top = new VBox(8, h, form, actions);
        setTop(top);
        setCenter(grid);

        runFilter();
    }

    private void runFilter() {
        Map<String, Object> body = new HashMap<>();
        if (!keyword.getText().isBlank())  body.put("keyword",        keyword.getText().trim());
        if (resolution.getValue() != null && !resolution.getValue().isBlank())
                                            body.put("resolution",     resolution.getValue());
        if (minDur.getValue() > 0)         body.put("minDurationSec", minDur.getValue());
        if (maxDur.getValue() > 0)         body.put("maxDurationSec", maxDur.getValue());
        if (premiumOnly.isSelected())      body.put("isPremium",      true);
        body.put("sortBy",        sortBy.getValue());
        body.put("sortDirection", sortDir.getValue());

        grid.setLoading();
        AsyncRunner.run(
                () -> AppContext.get().api().filterVideos(body, 0, 30),
                (PageResponse<Video> page) -> grid.setVideos(page.content));
    }
}
