package com.streamflix.service;

import com.streamflix.entity.Notification;
import com.streamflix.entity.User;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService            userService;

    public Notification create(Long userId, Notification.Type type,
                                String content, String linkUrl) {
        User user = userService.findById(userId);
        return notificationRepository.save(Notification.builder()
                .user(user).type(type).content(content)
                .linkUrl(linkUrl).isRead(false).build());
    }

    @Transactional(readOnly = true)
    public Page<Notification> list(Long userId, Pageable pageable) {
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserUserIdAndIsReadFalse(userId);
    }

    public void markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!n.getUser().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", notificationId);
        }
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    public void markAllRead(Long userId) {
        notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(n -> {
                    if (!Boolean.TRUE.equals(n.getIsRead())) {
                        n.setIsRead(true);
                        notificationRepository.save(n);
                    }
                });
    }
}
