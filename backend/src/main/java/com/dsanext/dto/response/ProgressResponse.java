package com.dsanext.dto.response;

import com.dsanext.domain.entity.Progress;
import com.dsanext.domain.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProgressResponse {

    private UUID id;
    private UUID problemId;
    private String problemTitle;
    private String problemSlug;
    private String problemTopic;
    private String problemDifficulty;
    private ProgressStatus status;
    private int attemptCount;
    private Instant firstAttemptedAt;
    private Instant solvedAt;
    private Instant updatedAt;

    public static ProgressResponse from(Progress progress) {
        return ProgressResponse.builder()
                .id(progress.getId())
                .problemId(progress.getProblem().getId())
                .problemTitle(progress.getProblem().getTitle())
                .problemSlug(progress.getProblem().getSlug())
                .problemTopic(progress.getProblem().getTopic())
                .problemDifficulty(progress.getProblem().getDifficulty().name())
                .status(progress.getStatus())
                .attemptCount(progress.getAttemptCount())
                .firstAttemptedAt(progress.getFirstAttemptedAt())
                .solvedAt(progress.getSolvedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}
