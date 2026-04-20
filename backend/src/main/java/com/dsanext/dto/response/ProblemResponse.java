package com.dsanext.dto.response;

import com.dsanext.domain.entity.Problem;
import com.dsanext.domain.enums.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProblemResponse {

    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String topic;
    private Difficulty difficulty;
    private String externalUrl;
    private PlatformResponse platform;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    // Contextual fields — populated when fetched with user context
    private String userProgressStatus;
    private boolean isBookmarked;
    private boolean hasNote;

    public static ProblemResponse from(Problem problem) {
        return ProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .slug(problem.getSlug())
                .description(problem.getDescription())
                .topic(problem.getTopic())
                .difficulty(problem.getDifficulty())
                .externalUrl(problem.getExternalUrl())
                .platform(problem.getPlatform() != null
                        ? PlatformResponse.from(problem.getPlatform()) : null)
                .isActive(problem.isActive())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .build();
    }
}
