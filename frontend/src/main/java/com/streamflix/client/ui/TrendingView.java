package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.util.AsyncRunner;

public class TrendingView extends VideoGridView {

    public TrendingView() {
        super("🔥 Trending Now");
        setLoading();
        AsyncRunner.run(
                () -> AppContext.get().api().trendingVideos(20),
                this::setVideos);
    }
}
