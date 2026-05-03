package com.streamflix.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Video {
    public Long   videoId;
    public Long   channelId;
    public String channelName;
    public String title;
    public String description;
    public String videoUrl;
    public String thumbnailUrl;
    public Integer durationSec;
    public String resolution;
    public Long   viewsCount;
    public Long   likesCount;
    public Long   dislikesCount;
    public String status;
    public Boolean isPremium;
    public String uploadDate;
    public List<String> categories;
    public List<String> tags;

    public String formattedDuration() {
        if (durationSec == null) return "—";
        int m = durationSec / 60;
        int s = durationSec % 60;
        return String.format("%d:%02d", m, s);
    }

    @Override public String toString() { return title; }
}
