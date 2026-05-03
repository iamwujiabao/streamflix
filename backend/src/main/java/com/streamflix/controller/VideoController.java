package com.streamflix.controller;

import com.streamflix.dto.*;
import com.streamflix.entity.VideoReaction;
import com.streamflix.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Videos", description = "Video content management, search and interaction")
public class VideoController {

    private final VideoService    videoService;
    private final WatchService    watchService;
    private final ReactionService reactionService;
    private final UserService     userService;

    @GetMapping
    @Operation(summary = "List published videos (newest first)")
    public ApiResponse<Page<VideoResponse>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadDate"));
        return ApiResponse.ok(videoService.listPublished(p));
    }

    @GetMapping("/search")
    @Operation(summary = "Keyword search across title and description")
    public ApiResponse<Page<VideoResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(videoService.search(q, PageRequest.of(page, size)));
    }

    @PostMapping("/filter")
    @Operation(summary = "Multi-criteria filter and sort")
    public ApiResponse<Page<VideoResponse>> filter(
            @RequestBody VideoFilterRequest req,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(videoService.filter(req, page, size));
    }

    @GetMapping("/trending")
    @Operation(summary = "Trending videos by engagement decay score")
    public ApiResponse<List<VideoResponse>> trending(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(videoService.trending(limit));
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<Page<VideoResponse>> byCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadDate"));
        return ApiResponse.ok(videoService.byCategory(categoryId, p));
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoResponse> get(@PathVariable Long videoId) {
        return ApiResponse.ok(videoService.findDtoById(videoId));
    }

    @PostMapping("/channel/{channelId}")
    public ApiResponse<VideoResponse> upload(
            @PathVariable Long channelId,
            @Valid @RequestBody VideoCreateRequest req) {
        return ApiResponse.ok("Video uploaded", videoService.uploadVideo(channelId, req));
    }

    @DeleteMapping("/{videoId}")
    public ApiResponse<Object> delete(@PathVariable Long videoId) {
        videoService.delete(videoId);
        return ApiResponse.ok("Video removed", null);
    }

    @PostMapping("/{videoId}/watch")
    public ApiResponse<Object> watch(
            @PathVariable Long videoId,
            @Valid @RequestBody WatchRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        watchService.record(userId, videoId, req);
        return ApiResponse.ok("Watch recorded", null);
    }

    @PostMapping("/{videoId}/like")
    public ApiResponse<String> like(@PathVariable Long videoId,
                                     @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(reactionService.react(userId, videoId, VideoReaction.Reaction.LIKE));
    }

    @PostMapping("/{videoId}/dislike")
    public ApiResponse<String> dislike(@PathVariable Long videoId,
                                        @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(reactionService.react(userId, videoId, VideoReaction.Reaction.DISLIKE));
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<VideoResponse>> recommendations(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(videoService.recommendations(userId, limit));
    }
}
