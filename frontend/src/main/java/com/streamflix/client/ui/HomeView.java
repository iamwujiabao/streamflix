package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.model.PageResponse;
import com.streamflix.client.model.Video;
import com.streamflix.client.util.AsyncRunner;

public class HomeView extends VideoGridView {

    public HomeView() {
        super("Browse Videos");
        setLoading();
        AsyncRunner.run(
                () -> AppContext.get().api().listVideos(0, 30),
                (PageResponse<Video> page) -> setVideos(page.content));
    }
}
