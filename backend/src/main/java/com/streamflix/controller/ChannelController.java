package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.dto.ChannelResponse;
import com.streamflix.service.ChannelService;
import com.streamflix.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;
    private final UserService    userService;

    @GetMapping
    public ApiResponse<List<ChannelResponse>> all() {
        return ApiResponse.ok(channelService.all());
    }

    @GetMapping("/{id}")
    public ApiResponse<ChannelResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(channelService.findDtoById(id));
    }

    @PostMapping
    public ApiResponse<ChannelResponse> create(
            @RequestBody Map<String,String> body,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok("Channel created",
                channelService.createChannel(userId,
                        body.get("channelName"), body.get("description")));
    }

    @PostMapping("/{channelId}/subscribe")
    public ApiResponse<String> subscribe(
            @PathVariable Long channelId,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(channelService.subscribe(userId, channelId));
    }
}
