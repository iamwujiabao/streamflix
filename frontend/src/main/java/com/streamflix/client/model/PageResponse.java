package com.streamflix.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Subset of Spring's Page<T> body that we actually consume. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {
    public List<T> content;
    public int     number;
    public int     size;
    public int     totalPages;
    public long    totalElements;
    public boolean last;
}
