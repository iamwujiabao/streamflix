package com.streamflix.service;

import com.streamflix.dto.VideoCreateRequest;
import com.streamflix.dto.VideoFilterRequest;
import com.streamflix.dto.VideoResponse;
import com.streamflix.entity.*;
import com.streamflix.exception.BadRequestException;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.streamflix.service.VideoSpecifications.*;

/**
 * IMPORTANT: spring.jpa.open-in-view is set to {@code false} (the recommended
 * production setting), so any traversal of LAZY associations on a Video — its
 * channel, categories, tags — must happen inside a @Transactional method.
 *
 * Public methods that controllers call therefore return DTOs (VideoResponse),
 * with the entity-to-DTO conversion done inside the transaction. The methods
 * that return raw {@link Video} entities (notably {@link #findById}) are
 * intended for internal use by other services that perform their own
 * transactional work on the entity.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VideoService {

    private final VideoRepository    videoRepository;
    private final ChannelRepository  channelRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository      tagRepository;

    public VideoResponse uploadVideo(Long channelId, VideoCreateRequest req) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", channelId));

        if (req.durationSec() == null || req.durationSec() <= 0)
            throw new BadRequestException("Duration must be > 0");

        Video v = Video.builder()
                .channel(channel)
                .title(req.title())
                .description(req.description())
                .videoUrl(req.videoUrl())
                .thumbnailUrl(req.thumbnailUrl())
                .durationSec(req.durationSec())
                .resolution(req.resolution() != null ? req.resolution() : "HD")
                .isPremium(Boolean.TRUE.equals(req.isPremium()))
                .status(Video.Status.PUBLISHED)
                .viewsCount(0L).likesCount(0L).dislikesCount(0L)
                .build();

        if (req.categoryIds() != null && !req.categoryIds().isEmpty()) {
            Set<Category> cats = new HashSet<>(categoryRepository.findAllById(req.categoryIds()));
            v.setCategories(cats);
        }
        if (req.tags() != null && !req.tags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String name : req.tags()) {
                Tag t = tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
                tags.add(t);
            }
            v.setTags(tags);
        }
        Video saved = videoRepository.save(v);
        return VideoResponse.fromEntity(saved);
    }

    public void delete(Long id) {
        Video v = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", id));
        v.setStatus(Video.Status.REMOVED);
        videoRepository.save(v);
    }

    @Transactional(readOnly = true)
    public Video findById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", id));
    }

    @Transactional(readOnly = true)
    public VideoResponse findDtoById(Long id) {
        return VideoResponse.fromEntity(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> listPublished(Pageable pageable) {
        return videoRepository.findByStatus(Video.Status.PUBLISHED, pageable)
                .map(VideoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return listPublished(pageable);
        return videoRepository.search(q.trim(), pageable)
                .map(VideoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> trending(int limit) {
        return videoRepository.findTrending(limit)
                .stream().map(VideoResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> recommendations(Long userId, int limit) {
        List<Video> recs = videoRepository.findRecommendations(userId, limit);
        if (recs.isEmpty()) recs = videoRepository.findTrending(limit);
        return recs.stream().map(VideoResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> byCategory(Integer categoryId, Pageable pageable) {
        return videoRepository.findByCategory(categoryId, pageable)
                .map(VideoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> filter(VideoFilterRequest req, int page, int size) {
        Specification<Video> spec = Specification
                .where(isPublished())
                .and(titleContains(req.keyword()))
                .and(hasCategoryId(req.categoryId()))
                .and(hasAnyTag(req.tags()))
                .and(durationBetween(req.minDurationSec(), req.maxDurationSec()))
                .and(resolutionEquals(req.resolution()))
                .and(isPremium(req.isPremium()))
                .and(channelIdEquals(req.channelId()))
                .and(uploadedAfter(req.uploadedAfter()))
                .and(uploadedBefore(req.uploadedBefore()));

        Sort sort = buildSort(req.sortBy(), req.sortDirection());
        return videoRepository.findAll(spec, PageRequest.of(page, size, sort))
                .map(VideoResponse::fromEntity);
    }

    private Sort buildSort(String sortBy, String dir) {
        String prop = switch (sortBy == null ? "" : sortBy.toLowerCase()) {
            case "views"      -> "viewsCount";
            case "likes"      -> "likesCount";
            case "duration"   -> "durationSec";
            case "title"      -> "title";
            default           -> "uploadDate";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(dir)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, prop);
    }
}
