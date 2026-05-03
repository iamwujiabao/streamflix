package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.dto.PlaylistDto;
import com.streamflix.dto.VideoResponse;
import com.streamflix.service.PlaylistService;
import com.streamflix.service.UserService;
import com.streamflix.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final UserService     userService;
    private final VideoService    videoService;

    @PostMapping
    public ApiResponse<PlaylistDto.Response> create(
            @Valid @RequestBody PlaylistDto.CreateRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok("Playlist created",
                playlistService.create(userId, req.title(), req.description(),
                        req.isPublic() == null || req.isPublic()));
    }

    @GetMapping("/me")
    public ApiResponse<List<PlaylistDto.Response>> myPlaylists(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(playlistService.listForUser(userId, false));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<PlaylistDto.Response>> publicForUser(@PathVariable Long userId) {
        return ApiResponse.ok(playlistService.listForUser(userId, true));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlaylistDto.Response> get(@PathVariable Long id) {
        return ApiResponse.ok(playlistService.findDtoById(id));
    }

    @GetMapping("/{id}/videos")
    public ApiResponse<List<VideoResponse>> videosIn(@PathVariable Long id) {
        return ApiResponse.ok(playlistService.getVideos(id).stream()
                .map(pv -> videoService.findDtoById(pv.getVideoId())).toList());
    }

    @PostMapping("/{id}/videos/{videoId}")
    public ApiResponse<Object> addVideo(@PathVariable Long id, @PathVariable Long videoId,
                                         @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        playlistService.addVideo(id, videoId, userId);
        return ApiResponse.ok("Video added", null);
    }

    @DeleteMapping("/{id}/videos/{videoId}")
    public ApiResponse<Object> removeVideo(@PathVariable Long id, @PathVariable Long videoId,
                                            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        playlistService.removeVideo(id, videoId, userId);
        return ApiResponse.ok("Video removed", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        playlistService.delete(id, userId);
        return ApiResponse.ok("Playlist deleted", null);
    }
}
