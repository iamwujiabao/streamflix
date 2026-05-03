package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.dto.UserResponse;
import com.streamflix.dto.WatchHistoryResponse;
import com.streamflix.entity.User;
import com.streamflix.service.UserService;
import com.streamflix.service.WatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService  userService;
    private final WatchService watchService;

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        User u = userService.findById(id);
        return ApiResponse.ok(UserResponse.fromEntity(u));
    }

    @GetMapping("/me/history")
    public ApiResponse<Page<WatchHistoryResponse>> myHistory(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        Pageable p  = PageRequest.of(page, size);
        return ApiResponse.ok(watchService.historyDto(userId, p));
    }
}
