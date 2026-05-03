package com.streamflix.dto;

import com.streamflix.entity.Channel;

/** Read view of a {@link Channel}. Eagerly resolves the owner's username,
 *  so {@link #fromEntity} must be called inside a Hibernate session
 *  (i.e. inside a {@code @Transactional} method) — see {@code application.yml}
 *  where {@code spring.jpa.open-in-view: false}. */
public record ChannelResponse(
        Long channelId,
        String name,
        String description,
        Long subscriberCount,
        String ownerUsername
) {
    public static ChannelResponse fromEntity(Channel c) {
        return new ChannelResponse(
                c.getChannelId(),
                c.getChannelName(),
                c.getDescription(),
                c.getSubscriberCount(),
                c.getOwner() != null ? c.getOwner().getUsername() : null);
    }
}
