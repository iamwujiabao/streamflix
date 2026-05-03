package com.streamflix.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiEnvelope<T> {
    public boolean success;
    public String  message;
    public T       data;
}
