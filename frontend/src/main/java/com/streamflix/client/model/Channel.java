package com.streamflix.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Channel {
    public Long   channelId;
    public String name;
    public String description;
    public Long   subscriberCount;
    public String ownerUsername;

    @Override public String toString() {
        return name + " (" + (subscriberCount == null ? 0 : subscriberCount) + ")";
    }
}
