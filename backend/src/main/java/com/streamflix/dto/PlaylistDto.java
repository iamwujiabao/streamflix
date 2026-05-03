package com.streamflix.dto;

import com.streamflix.entity.Playlist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PlaylistDto {

    public record CreateRequest(
            @NotBlank @Size(max = 150) String title,
            String description,
            Boolean isPublic
    ) {}

    public record Response(
            Long playlistId,
            Long ownerUserId,
            String ownerUsername,
            String title,
            String description,
            Boolean isPublic,
            LocalDateTime createdAt
    ) {
        public static Response fromEntity(Playlist p) {
            return new Response(
                    p.getPlaylistId(),
                    p.getUser().getUserId(),
                    p.getUser().getUsername(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getIsPublic(),
                    p.getCreatedAt());
        }
    }
}
