package com.dsanext.service;

import com.dsanext.TestDataFactory;
import com.dsanext.domain.entity.*;
import com.dsanext.domain.enums.Difficulty;
import com.dsanext.dto.request.CreateProblemRequest;
import com.dsanext.dto.request.UpdateProblemRequest;
import com.dsanext.dto.response.ProblemResponse;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.repository.*;
import com.dsanext.util.SlugUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProblemService Unit Tests")
class ProblemServiceTest {

    @Mock private ProblemRepository  problemRepository;
    @Mock private PlatformRepository platformRepository;
    @Mock private ProgressRepository progressRepository;
    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private NoteRepository     noteRepository;
    @Mock private ActivityLogService activityLogService;

    @Spy  private SlugUtils slugUtils = new SlugUtils();

    @InjectMocks private ProblemService problemService;

    private User    admin;
    private Platform platform;
    private Problem  problem;

    @BeforeEach
    void setUp() {
        admin    = TestDataFactory.buildAdmin();
        platform = TestDataFactory.buildPlatform();
        problem  = TestDataFactory.buildEasyProblem(platform, admin);
    }

    // ── Create Problem ─────────────────────────────────────────

    @Test
    @DisplayName("createProblem — success: saves problem with generated slug")
    void createProblem_success() {
        CreateProblemRequest request = TestDataFactory.buildCreateProblemRequest();
        request.setPlatformId(platform.getId());

        when(problemRepository.existsBySlug(anyString())).thenReturn(false);
        when(platformRepository.findById(platform.getId())).thenReturn(Optional.of(platform));
        when(problemRepository.save(any(Problem.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        ProblemResponse response = problemService.createProblem(request, admin);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getSlug()).isEqualTo("new-problem");
        assertThat(response.getDifficulty()).isEqualTo(Difficulty.EASY);

        verify(problemRepository).save(argThat(p ->
                p.getTitle().equals(request.getTitle()) &&
                p.getDifficulty() == Difficulty.EASY &&
                p.isActive()
        ));
    }

    @Test
    @DisplayName("createProblem — slug collision: appends numeric suffix")
    void createProblem_slugCollision_appendsSuffix() {
        CreateProblemRequest request = TestDataFactory.buildCreateProblemRequest();

        // First slug taken, second available
        when(problemRepository.existsBySlug("new-problem")).thenReturn(true);
        when(problemRepository.existsBySlug("new-problem-2")).thenReturn(false);
        when(problemRepository.save(any(Problem.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        ProblemResponse response = problemService.createProblem(request, admin);

        assertThat(response.getSlug()).isEqualTo("new-problem-2");
    }

    @Test
    @DisplayName("createProblem — invalid platformId: throws ResourceNotFoundException")
    void createProblem_invalidPlatform_throwsException() {
        CreateProblemRequest request = TestDataFactory.buildCreateProblemRequest();
        UUID fakePlatformId = UUID.randomUUID();
        request.setPlatformId(fakePlatformId);

        when(problemRepository.existsBySlug(anyString())).thenReturn(false);
        when(platformRepository.findById(fakePlatformId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.createProblem(request, admin))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Platform");
    }

    // ── Get Problem ────────────────────────────────────────────

    @Test
    @DisplayName("getProblemBySlug — success: returns problem for valid slug")
    void getProblemBySlug_success() {
        when(problemRepository.findBySlug("two-sum")).thenReturn(Optional.of(problem));

        ProblemResponse response = problemService.getProblemBySlug("two-sum");

        assertThat(response.getSlug()).isEqualTo("two-sum");
        assertThat(response.getDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(response.getTopic()).isEqualTo("Array");
    }

    @Test
    @DisplayName("getProblemBySlug — not found: throws ResourceNotFoundException")
    void getProblemBySlug_notFound_throwsException() {
        when(problemRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.getProblemBySlug("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("slug");
    }

    // ── Update Problem ─────────────────────────────────────────

    @Test
    @DisplayName("updateProblem — success: updates provided fields only")
    void updateProblem_partialUpdate_success() {
        UpdateProblemRequest request = new UpdateProblemRequest();
        request.setDifficulty(Difficulty.HARD);
        request.setIsActive(false);

        when(problemRepository.findById(problem.getId())).thenReturn(Optional.of(problem));
        when(problemRepository.save(any(Problem.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        ProblemResponse response = problemService.updateProblem(problem.getId(), request, admin);

        assertThat(response.getDifficulty()).isEqualTo(Difficulty.HARD);
        assertThat(response.isActive()).isFalse();
        assertThat(response.getTitle()).isEqualTo(problem.getTitle()); // unchanged
    }

    // ── Delete Problem ─────────────────────────────────────────

    @Test
    @DisplayName("deleteProblem — success: calls repository delete")
    void deleteProblem_success() {
        when(problemRepository.findById(problem.getId())).thenReturn(Optional.of(problem));
        doNothing().when(problemRepository).delete(problem);
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        problemService.deleteProblem(problem.getId(), admin);

        verify(problemRepository).delete(problem);
        verify(activityLogService).log(eq(admin), eq("PROBLEM_DELETED"), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("deleteProblem — not found: throws ResourceNotFoundException")
    void deleteProblem_notFound_throwsException() {
        UUID badId = UUID.randomUUID();
        when(problemRepository.findById(badId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.deleteProblem(badId, admin))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(problemRepository, never()).delete(any());
    }

    // ── User Context Enrichment ────────────────────────────────

    @Test
    @DisplayName("getProblemBySlugForUser — enriches with user progress and bookmark status")
    void getProblemBySlugForUser_enrichesUserContext() {
        UUID userId = UUID.randomUUID();
        Progress progress = TestDataFactory.buildSolvedProgress(admin, problem);

        when(problemRepository.findBySlug("two-sum")).thenReturn(Optional.of(problem));
        when(progressRepository.findByUserIdAndProblemId(userId, problem.getId()))
                .thenReturn(Optional.of(progress));
        when(bookmarkRepository.existsByUserIdAndProblemId(userId, problem.getId()))
                .thenReturn(true);
        when(noteRepository.existsByUserIdAndProblemId(userId, problem.getId()))
                .thenReturn(false);

        ProblemResponse response = problemService.getProblemBySlugForUser("two-sum", userId);

        assertThat(response.getUserProgressStatus()).isEqualTo("SOLVED");
        assertThat(response.isBookmarked()).isTrue();
        assertThat(response.isHasNote()).isFalse();
    }
}
