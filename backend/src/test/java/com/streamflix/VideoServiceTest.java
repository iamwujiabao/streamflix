package com.streamflix;

import com.streamflix.dto.VideoCreateRequest;
import com.streamflix.dto.VideoFilterRequest;
import com.streamflix.entity.Channel;
import com.streamflix.entity.User;
import com.streamflix.entity.Video;
import com.streamflix.repository.ChannelRepository;
import com.streamflix.repository.UserRepository;
import com.streamflix.repository.VideoRepository;
import com.streamflix.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VideoServiceTest {

    @Autowired private VideoService       videoService;
    @Autowired private VideoRepository    videoRepository;
    @Autowired private ChannelRepository  channelRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private PasswordEncoder    passwordEncoder;

    private Channel channel;
    private String  uniq;

    @BeforeEach
    void setUp() {
        uniq = UUID.randomUUID().toString().substring(0, 8);
        User owner = userRepository.save(User.builder()
                .username("creator_" + uniq).email("c_" + uniq + "@x.com")
                .passwordHash(passwordEncoder.encode("pw")).role(User.Role.CREATOR)
                .isActive(true).build());

        channel = channelRepository.save(Channel.builder()
                .owner(owner).channelName("ch_" + uniq).description("Test")
                .subscriberCount(0L).build());
    }

    @Test
    void upload_creates_a_published_video() {
        VideoCreateRequest req = new VideoCreateRequest(
                "My first video " + uniq, "A description here",
                "https://cdn/x.mp4", "https://cdn/x.jpg", 600,
                "HD", false, List.of(), List.of("intro_" + uniq, "demo_" + uniq));
        Video saved = videoService.uploadVideo(channel.getChannelId(), req);

        assertNotNull(saved.getVideoId());
        assertEquals(Video.Status.PUBLISHED, saved.getStatus());
        assertEquals(2, saved.getTags().size());
    }

    @Test
    void filter_by_keyword_returns_only_matching_videos() {
        String marker = "ZZZ" + uniq;
        upload(marker + " Spring Boot Tutorial", "Learn Spring", 600);
        upload("Java Streams Deep Dive " + uniq, "Functional", 900);
        upload("Cooking Pho " + uniq, "Soup", 720);

        VideoFilterRequest req = new VideoFilterRequest(
                marker, null, null, null, null, null, null, null,
                null, null, "upload_date", "desc");
        Page<Video> result = videoService.filter(req, 0, 20);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getTitle().contains("Spring Boot"));
    }

    @Test
    void filter_by_duration_range() {
        String marker = "DUR" + uniq;
        upload(marker + "_short", "60s", 60);
        upload(marker + "_mid",   "10min", 600);
        upload(marker + "_long",  "2hr",   7200);

        VideoFilterRequest req = new VideoFilterRequest(
                marker, null, null, 100, 1000, null, null, null,
                null, null, null, null);
        Page<Video> result = videoService.filter(req, 0, 20);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getTitle().endsWith("_mid"));
    }

    @Test
    void delete_marks_status_as_removed() {
        Video v = upload("ToRemove_" + uniq, "x", 100);
        videoService.delete(v.getVideoId());

        Video reloaded = videoRepository.findById(v.getVideoId()).orElseThrow();
        assertEquals(Video.Status.REMOVED, reloaded.getStatus());
    }

    private Video upload(String title, String desc, int duration) {
        return videoService.uploadVideo(channel.getChannelId(),
                new VideoCreateRequest(title, desc, "https://cdn/" + title + ".mp4", null,
                        duration, "HD", false, List.of(), List.of()));
    }
}
