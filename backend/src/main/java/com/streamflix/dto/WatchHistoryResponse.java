package com.streamflix.dto;

import com.streamflix.entity.WatchHistory;

import java.time.LocalDateTime;

/** Read view of a {@link WatchHistory} entry. Touches the underlying Video,
 *  so {@link #fromEntity} must be invoked inside a Hibernate session. */
public record WatchHistoryResponse(
        Long historyId,
        VideoResponse video,
        Integer watchDuration,
        LocalDateTime watchedAt,
        String deviceType
) {
    public static WatchHistoryResponse fromEntity(WatchHistory h) {
        return new WatchHistoryResponse(
                h.getHistoryId(),
                VideoResponse.fromEntity(h.getVideo()),
                h.getWatchDuration(),
                h.getWatchedAt(),
                h.getDeviceType());
    }
}
