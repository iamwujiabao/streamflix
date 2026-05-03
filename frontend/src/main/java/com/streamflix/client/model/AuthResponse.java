package com.streamflix.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    public String token;
    public String tokenType;
    public long   expiresInMs;
    public User   user;
}
