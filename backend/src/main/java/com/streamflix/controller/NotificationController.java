package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.dto.NotificationDto;
import com.streamflix.service.NotificationService;
import com.streamflix.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService         userService;

    @GetMapping
    public ApiResponse<Page<NotificationDto>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(notificationService.list(userId, PageRequest.of(page, size))
                .map(NotificationDto::fromEntity));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unread(@AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(Map.of("count", notificationService.unreadCount(userId)));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Object> markRead(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        notificationService.markRead(id, userId);
        return ApiResponse.ok("Marked as read", null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Object> markAllRead(@AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        notificationService.markAllRead(userId);
        return ApiResponse.ok("All marked as read", null);
    }
}
