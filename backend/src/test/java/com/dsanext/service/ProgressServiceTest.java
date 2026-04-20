package com.dsanext.service;

import com.dsanext.TestDataFactory;
import com.dsanext.domain.entity.*;
import com.dsanext.domain.enums.ProgressStatus;
import com.dsanext.dto.request.UpdateProgressRequest;
import com.dsanext.dto.response.ProgressResponse;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.repository.ProgressRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProgressService Unit Tests")
class ProgressServiceTest {

    @Mock private ProgressRepository progressRepository;
    @Mock private ProblemService     problemService;
    @Mock private ActivityLogService activityLogService;

    @InjectMocks private ProgressService progressService;

    private User    user;
    private Problem problem;

    @BeforeEach
    void setUp() {
        user    = TestDataFactory.buildUser();
        problem = TestDataFactory.buildEasyProblem(TestDataFactory.buildPlatform(), user);
    }

    // ── Upsert — Create ───────────────────────────────────────

    @Test
    @DisplayName("upsertProgress — create: new progress record when none exists")
    void upsertProgress_create_newRecord() {
        UpdateProgressRequest req = new UpdateProgressRequest();
        req.setStatus(ProgressStatus.IN_PROGRESS);

        when(problemService.findById(problem.getId())).thenReturn(problem);
        when(progressRepository.findByUserIdAndProblemId(user.getId(), problem.getId()))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        ProgressResponse response = progressService.upsertProgress(
                user.getId(), problem.getId(), req, user);

        assertThat(response.getStatus()).isEqualTo(ProgressStatus.IN_PROGRESS);

        verify(progressRepository).save(argThat(p ->
                p.getStatus() == ProgressStatus.IN_PROGRESS &&
                p.getAttemptCount() == 1 &&
                p.getFirstAttemptedAt() != null
        ));
    }

    @Test
    @DisplayName("upsertProgress — mark solved: sets solvedAt timestamp")
    void upsertProgress_markSolved_setsSolvedAt() {
        UpdateProgressRequest req = new UpdateProgressRequest();
        req.setStatus(ProgressStatus.SOLVED);

        Progress existing = Progress.builder()
                .id(UUID.randomUUID())
                .user(user).problem(problem)
                .status(ProgressStatus.IN_PROGRESS)
                .attemptCount(2)
                .build();

        when(problemService.findById(problem.getId())).thenReturn(problem);
        when(progressRepository.findByUserIdAndProblemId(user.getId(), problem.getId()))
                .thenReturn(Optional.of(existing));
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        ProgressResponse response = progressService.upsertProgress(
                user.getId(), problem.getId(), req, user);

        assertThat(response.getStatus()).isEqualTo(ProgressStatus.SOLVED);

        verify(progressRepository).save(argThat(p ->
                p.getStatus() == ProgressStatus.SOLVED &&
                p.getSolvedAt() != null
        ));
    }

    @Test
    @DisplayName("upsertProgress — revisit: does not increment attempt count")
    void upsertProgress_revisit_doesNotIncrementAttempts() {
        UpdateProgressRequest req = new UpdateProgressRequest();
        req.setStatus(ProgressStatus.REVISIT);

        Progress existing = Progress.builder()
                .id(UUID.randomUUID())
                .user(user).problem(problem)
                .status(ProgressStatus.SOLVED)
                .attemptCount(3)
                .build();

        when(problemService.findById(problem.getId())).thenReturn(problem);
        when(progressRepository.findByUserIdAndProblemId(user.getId(), problem.getId()))
                .thenReturn(Optional.of(existing));
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        progressService.upsertProgress(user.getId(), problem.getId(), req, user);

        verify(progressRepository).save(argThat(p ->
                p.getAttemptCount() == 3  // unchanged
        ));
    }

    // ── Progress Domain Logic ─────────────────────────────────

    @Test
    @DisplayName("Progress.recordAttempt — increments count and sets firstAttemptedAt")
    void progressDomain_recordAttempt_incrementsAndSetsTimestamp() {
        Progress progress = TestDataFactory.buildProgress(user, problem);

        assertThat(progress.getAttemptCount()).isZero();
        assertThat(progress.getFirstAttemptedAt()).isNull();

        progress.recordAttempt();

        assertThat(progress.getAttemptCount()).isEqualTo(1);
        assertThat(progress.getFirstAttemptedAt()).isNotNull();

        // Second call increments count but does NOT reset firstAttemptedAt
        var firstTime = progress.getFirstAttemptedAt();
        progress.recordAttempt();

        assertThat(progress.getAttemptCount()).isEqualTo(2);
        assertThat(progress.getFirstAttemptedAt()).isEqualTo(firstTime);
    }

    @Test
    @DisplayName("Progress.markSolved — sets status and solvedAt, idempotent on second call")
    void progressDomain_markSolved_idempotent() {
        Progress progress = TestDataFactory.buildProgress(user, problem);
        progress.recordAttempt();

        progress.markSolved();
        var solvedAt = progress.getSolvedAt();

        assertThat(progress.getStatus()).isEqualTo(ProgressStatus.SOLVED);
        assertThat(solvedAt).isNotNull();

        // Second call should not update solvedAt
        progress.markSolved();
        assertThat(progress.getSolvedAt()).isEqualTo(solvedAt);
    }

    // ── Stats ─────────────────────────────────────────────────

    @Test
    @DisplayName("countSolved — delegates to repository correctly")
    void countSolved_delegatesToRepository() {
        when(progressRepository.countByUserIdAndStatus(user.getId(), ProgressStatus.SOLVED))
                .thenReturn(42L);

        assertThat(progressService.countSolved(user.getId())).isEqualTo(42L);
    }
}
