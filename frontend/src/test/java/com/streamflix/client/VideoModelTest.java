package com.streamflix.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamflix.client.model.Video;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VideoModelTest {

    @Test
    void formattedDuration_handles_minutes_and_seconds() {
        Video v = new Video();
        v.durationSec = 125;
        assertEquals("2:05", v.formattedDuration());
    }

    @Test
    void formattedDuration_returns_dash_for_null() {
        assertEquals("—", new Video().formattedDuration());
    }

    @Test
    void deserialises_unknown_fields_without_failing() throws Exception {
        String json = """
                {"videoId":1,"title":"Hello","durationSec":60,
                 "unknownField":"oops","categories":["Music","Tech"]}
                """;
        Video v = new ObjectMapper().readValue(json, Video.class);
        assertEquals(1L, v.videoId);
        assertEquals("Hello", v.title);
        assertEquals(60, v.durationSec);
        assertEquals(2, v.categories.size());
    }
}
