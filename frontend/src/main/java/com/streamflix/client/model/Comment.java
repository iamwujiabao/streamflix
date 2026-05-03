package com.streamflix.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {
    public Long   commentId;
    public Long   videoId;
    public Long   userId;
    public String username;
    public Long   parentCommentId;
    public String content;
    public Integer likesCount;
    public String createdAt;
}
