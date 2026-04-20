package com.dsanext.service;

import com.dsanext.domain.entity.Problem;
import com.dsanext.domain.entity.Progress;
import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.ProgressStatus;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.UpdateProgressRequest;
import com.dsanext.dto.response.ProgressResponse;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final ProblemService     problemService;
    private final ActivityLogService activityLogService;

    // ── User progress queries ────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ProgressResponse> getUserProgress(UUID userId,
            ProgressStatus status, Pageable pageable) {
        return PageResponse.from(
                progressRepository.findByUserIdFiltered(userId, status, pageable)
                        .map(ProgressResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgressForProblem(UUID userId, UUID problemId) {
        Progress progress = progressRepository.findByUserIdAndProblemId(userId, problemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Progress", "userId+problemId", userId + "+" + problemId));
        return ProgressResponse.from(progress);
    }

    // ── Upsert progress ──────────────────────────────────────

    /**
     * Create or update progress for a user on a problem.
     * If no record exists, it is created. Existing records are updated.
     * Automatically increments attempt count when status moves to IN_PROGRESS or SOLVED.
     */
    @Transactional
    public ProgressResponse upsertProgress(UUID userId, UUID problemId,
            UpdateProgressRequest request, User user) {

        Problem problem = problemService.findById(problemId);

        Progress progress = progressRepository
                .findByUserIdAndProblemId(userId, problemId)
                .orElseGet(() -> Progress.builder()
                        .user(user)
                        .problem(problem)
                        .status(ProgressStatus.NOT_STARTED)
                        .build());

        ProgressStatus newStatus = request.getStatus();
        ProgressStatus oldStatus = progress.getStatus();

        // Auto-increment attempts when moving to active states
        if (newStatus == ProgressStatus.IN_PROGRESS || newStatus == ProgressStatus.SOLVED) {
            if (oldStatus == ProgressStatus.NOT_STARTED || oldStatus == ProgressStatus.REVISIT) {
                progress.recordAttempt();
            }
        }

        // Use domain method when marking solved — sets solvedAt timestamp
        if (newStatus == ProgressStatus.SOLVED) {
            progress.markSolved();
        } else {
            progress.setStatus(newStatus);
        }

        Progress saved = progressRepository.save(progress);

        activityLogService.log(user, "PROGRESS_UPDATED", "PROBLEM", problemId.toString(),
                Map.of("status", newStatus.name(), "problemTitle", problem.getTitle()));

        log.debug("Progress upserted: user={} problem={} status={}", userId, problemId, newStatus);
        return ProgressResponse.from(saved);
    }

    // ── Stats ────────────────────────────────────────────────

    public long countSolved(UUID userId) {
        return progressRepository.countByUserIdAndStatus(userId, ProgressStatus.SOLVED);
    }

    public long countInProgress(UUID userId) {
        return progressRepository.countByUserIdAndStatus(userId, ProgressStatus.IN_PROGRESS);
    }

    public long countRevisit(UUID userId) {
        return progressRepository.countByUserIdAndStatus(userId, ProgressStatus.REVISIT);
    }
}
