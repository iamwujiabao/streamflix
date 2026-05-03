package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.util.AsyncRunner;

public class RecommendedView extends VideoGridView {

    public RecommendedView() {
        super("✨ Recommended For You");
        setLoading();
        AsyncRunner.run(
                () -> AppContext.get().api().recommendations(20),
                this::setVideos);
    }
}
