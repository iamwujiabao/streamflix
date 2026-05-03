package com.streamflix.service;

import com.streamflix.entity.Video;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Composable JPA Criteria predicates that drive the
 * {@code GET /videos/filter} endpoint.
 */
public final class VideoSpecifications {

    private VideoSpecifications() {}

    public static Specification<Video> isPublished() {
        return (root, q, cb) -> cb.equal(root.get("status"), Video.Status.PUBLISHED);
    }

    public static Specification<Video> titleContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")),       pattern),
                cb.like(cb.lower(root.get("description")), pattern));
    }

    public static Specification<Video> hasCategoryId(Integer categoryId) {
        if (categoryId == null) return null;
        return (root, q, cb) -> {
            if (q != null) q.distinct(true);
            return cb.equal(root.join("categories", JoinType.INNER).get("categoryId"), categoryId);
        };
    }

    public static Specification<Video> hasAnyTag(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return (root, q, cb) -> {
            if (q != null) q.distinct(true);
            return root.join("tags", JoinType.INNER).get("name").in(tags);
        };
    }

    public static Specification<Video> durationBetween(Integer minSec, Integer maxSec) {
        if (minSec == null && maxSec == null) return null;
        return (root, q, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (minSec != null) preds.add(cb.greaterThanOrEqualTo(root.get("durationSec"), minSec));
            if (maxSec != null) preds.add(cb.lessThanOrEqualTo(root.get("durationSec"), maxSec));
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    public static Specification<Video> resolutionEquals(String resolution) {
        if (resolution == null || resolution.isBlank()) return null;
        return (root, q, cb) -> cb.equal(root.get("resolution"), resolution);
    }

    public static Specification<Video> uploadedAfter(LocalDateTime after) {
        if (after == null) return null;
        return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("uploadDate"), after);
    }

    public static Specification<Video> uploadedBefore(LocalDateTime before) {
        if (before == null) return null;
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("uploadDate"), before);
    }

    public static Specification<Video> isPremium(Boolean premium) {
        if (premium == null) return null;
        return (root, q, cb) -> cb.equal(root.get("isPremium"), premium);
    }

    public static Specification<Video> channelIdEquals(Long channelId) {
        if (channelId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("channel").get("channelId"), channelId);
    }
}
