package com.streamflix.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Multi-criteria search/filter request. All fields are optional —
 * leave blank to skip that criterion.
 */
public record VideoFilterRequest(
        String         keyword,
        Integer        categoryId,
        List<String>   tags,
        Integer        minDurationSec,
        Integer        maxDurationSec,
        String         resolution,
        Boolean        isPremium,
        Long           channelId,
        LocalDateTime  uploadedAfter,
        LocalDateTime  uploadedBefore,
        String         sortBy,        // upload_date | views | likes | duration
        String         sortDirection  // asc | desc
) {}
