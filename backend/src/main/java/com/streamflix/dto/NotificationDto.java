package com.streamflix.dto;

import com.streamflix.entity.Notification;

import java.time.LocalDateTime;

public record NotificationDto(
        Long notificationId,
        String type,
        String content,
        String linkUrl,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationDto fromEntity(Notification n) {
        return new NotificationDto(
                n.getNotificationId(),
                n.getType().name(),
                n.getContent(),
                n.getLinkUrl(),
                n.getIsRead(),
                n.getCreatedAt());
    }
}
