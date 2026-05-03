package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.model.PageResponse;
import com.streamflix.client.model.Video;
import com.streamflix.client.util.AsyncRunner;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryView extends VideoGridView {

    public HistoryView() {
        super("⏱ Watch History");
        setLoading();
        AsyncRunner.run(
                () -> AppContext.get().api().myHistory(0, 30),
                (PageResponse<Map<String, Object>> page) -> {
                    List<Video> videos = new ArrayList<>();
                    if (page.content != null) {
                        ObjectMapper m = new ObjectMapper();
                        for (Map<String, Object> row : page.content) {
                            Object v = row.get("video");
                            if (v != null) {
                                videos.add(m.convertValue(v, Video.class));
                            }
                        }
                    }
                    setVideos(videos);
                });
    }
}
