package com.streamflix.service;

import com.streamflix.dto.PlaylistDto;
import com.streamflix.entity.*;
import com.streamflix.exception.BadRequestException;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.PlaylistRepository;
import com.streamflix.repository.PlaylistVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    private final PlaylistRepository      playlistRepository;
    private final PlaylistVideoRepository playlistVideoRepository;
    private final UserService             userService;
    private final VideoService            videoService;

    public PlaylistDto.Response create(Long userId, String title, String description, boolean isPublic) {
        User u = userService.findById(userId);
        Playlist p = playlistRepository.save(Playlist.builder()
                .user(u).title(title).description(description).isPublic(isPublic).build());
        return PlaylistDto.Response.fromEntity(p);
    }

    @Transactional(readOnly = true)
    public Playlist findById(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", id));
    }

    @Transactional(readOnly = true)
    public PlaylistDto.Response findDtoById(Long id) {
        return PlaylistDto.Response.fromEntity(findById(id));
    }

    @Transactional(readOnly = true)
    public List<PlaylistDto.Response> listForUser(Long userId, boolean publicOnly) {
        List<Playlist> playlists = publicOnly
                ? playlistRepository.findByUserUserIdAndIsPublicTrue(userId)
                : playlistRepository.findByUserUserId(userId);
        return playlists.stream().map(PlaylistDto.Response::fromEntity).toList();
    }

    public PlaylistVideo addVideo(Long playlistId, Long videoId, Long requestingUserId) {
        Playlist playlist = findById(playlistId);
        if (!playlist.getUser().getUserId().equals(requestingUserId))
            throw new BadRequestException("Only the playlist owner can add videos");

        videoService.findById(videoId);
        int next = playlistVideoRepository.findMaxPosition(playlistId) + 1;
        return playlistVideoRepository.save(PlaylistVideo.builder()
                .playlistId(playlistId).videoId(videoId).position(next).build());
    }

    public void removeVideo(Long playlistId, Long videoId, Long requestingUserId) {
        Playlist playlist = findById(playlistId);
        if (!playlist.getUser().getUserId().equals(requestingUserId))
            throw new BadRequestException("Only the playlist owner can remove videos");
        playlistVideoRepository.deleteById(new PlaylistVideo.PlaylistVideoId(playlistId, videoId));
    }

    @Transactional(readOnly = true)
    public List<PlaylistVideo> getVideos(Long playlistId) {
        return playlistVideoRepository.findByPlaylist(playlistId);
    }

    public void delete(Long playlistId, Long requestingUserId) {
        Playlist p = findById(playlistId);
        if (!p.getUser().getUserId().equals(requestingUserId))
            throw new BadRequestException("Only the owner can delete a playlist");
        playlistRepository.deleteById(playlistId);
    }
}
