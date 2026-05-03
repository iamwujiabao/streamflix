package com.streamflix;

import com.streamflix.dto.PlaylistDto;
import com.streamflix.dto.VideoCreateRequest;
import com.streamflix.dto.VideoResponse;
import com.streamflix.entity.*;
import com.streamflix.exception.BadRequestException;
import com.streamflix.repository.*;
import com.streamflix.service.PlaylistService;
import com.streamflix.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaylistServiceTest {

    @Autowired private PlaylistService           playlistService;
    @Autowired private VideoService              videoService;
    @Autowired private UserRepository            userRepo;
    @Autowired private ChannelRepository         channelRepo;
    @Autowired private PasswordEncoder           encoder;

    private User user;
    private VideoResponse video1, video2;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        user = userRepo.save(User.builder()
                .username("plu_" + suffix).email("p_" + suffix + "@x.com")
                .passwordHash(encoder.encode("pw")).role(User.Role.USER)
                .isActive(true).build());

        User creator = userRepo.save(User.builder()
                .username("plc_" + suffix).email("c_" + suffix + "@x.com")
                .passwordHash(encoder.encode("pw")).role(User.Role.CREATOR)
                .isActive(true).build());

        Channel ch = channelRepo.save(Channel.builder()
                .owner(creator).channelName("ch_" + suffix)
                .subscriberCount(0L).build());

        video1 = videoService.uploadVideo(ch.getChannelId(),
                new VideoCreateRequest("V1_" + suffix, "d", "u1", null, 100, "HD", false, List.of(), List.of()));
        video2 = videoService.uploadVideo(ch.getChannelId(),
                new VideoCreateRequest("V2_" + suffix, "d", "u2", null, 200, "HD", false, List.of(), List.of()));
    }

    @Test
    void create_and_add_videos_increments_position() {
        PlaylistDto.Response p = playlistService.create(user.getUserId(), "My List", "desc", true);
        playlistService.addVideo(p.playlistId(), video1.videoId(), user.getUserId());
        playlistService.addVideo(p.playlistId(), video2.videoId(), user.getUserId());

        List<PlaylistVideo> videos = playlistService.getVideos(p.playlistId());
        assertEquals(2, videos.size());
        assertEquals(1, videos.get(0).getPosition());
        assertEquals(2, videos.get(1).getPosition());
    }

    @Test
    void non_owner_cannot_add_video() {
        PlaylistDto.Response p = playlistService.create(user.getUserId(), "My List", null, true);
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User intruder = userRepo.save(User.builder()
                .username("int_" + suffix).email("int_" + suffix + "@x.com")
                .passwordHash(encoder.encode("pw")).role(User.Role.USER)
                .isActive(true).build());

        assertThrows(BadRequestException.class, () ->
                playlistService.addVideo(p.playlistId(), video1.videoId(), intruder.getUserId()));
    }

    @Test
    void remove_video_works_for_owner() {
        PlaylistDto.Response p = playlistService.create(user.getUserId(), "My List", null, true);
        playlistService.addVideo(p.playlistId(), video1.videoId(), user.getUserId());
        playlistService.removeVideo(p.playlistId(), video1.videoId(), user.getUserId());

        assertEquals(0, playlistService.getVideos(p.playlistId()).size());
    }
}
