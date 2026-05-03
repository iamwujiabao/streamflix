package com.streamflix.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public Long   userId;
    public String username;
    public String email;
    public String fullName;
    public String country;
    public String role;
    public Boolean isActive;
}
