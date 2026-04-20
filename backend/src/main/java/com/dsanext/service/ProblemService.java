package com.dsanext.service;

import com.dsanext.domain.entity.Platform;
import com.dsanext.domain.entity.Problem;
import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.Difficulty;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.CreateProblemRequest;
import com.dsanext.dto.request.UpdateProblemRequest;
import com.dsanext.dto.response.ProblemResponse;
import com.dsanext.exception.DuplicateResourceException;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.repository.BookmarkRepository;
import com.dsanext.repository.NoteRepository;
import com.dsanext.repository.PlatformRepository;
import com.dsanext.repository.ProblemRepository;
import com.dsanext.repository.ProgressRepository;
import com.dsanext.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository  problemRepository;
    private final PlatformRepository platformRepository;
    private final ProgressRepository progressRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NoteRepository     noteRepository;
    private final SlugUtils          slugUtils;
    private final ActivityLogService activityLogService;

    // ── Public problem listing ───────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ProblemResponse> getProblems(String search, Difficulty difficulty,
            String topic, UUID platformId, Pageable pageable) {
        return PageResponse.from(
                problemRepository.findAllFiltered(search, difficulty, topic, platformId, pageable)
                        .map(ProblemResponse::from)
        );
    }

    /**
     * Get problems enriched with user context — progress status, bookmark, note flags.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProblemResponse> getProblemsForUser(String search, Difficulty difficulty,
            String topic, UUID platformId, UUID userId, Pageable pageable) {
        return PageResponse.from(
                problemRepository.findAllFiltered(search, difficulty, topic, platformId, pageable)
                        .map(problem -> enrichWithUserContext(problem, userId))
        );
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblemBySlug(String slug) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "slug", slug));
        return ProblemResponse.from(problem);
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblemBySlugForUser(String slug, UUID userId) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "slug", slug));
        return enrichWithUserContext(problem, userId);
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblemById(UUID id) {
        return ProblemResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctTopics() {
        return problemRepository.findDistinctTopics();
    }

    // ── Admin CRUD ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ProblemResponse> getProblemsAdmin(String search, Difficulty difficulty,
            String topic, Boolean active, Pageable pageable) {
        return PageResponse.from(
                problemRepository.findAllAdmin(search, difficulty, topic, active, pageable)
                        .map(ProblemResponse::from)
        );
    }

    @Transactional
    public ProblemResponse createProblem(CreateProblemRequest request, User admin) {
        // Generate unique slug
        String baseSlug = slugUtils.toSlug(request.getTitle());
        String slug = baseSlug;
        int suffix = 2;
        while (problemRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + suffix++;
        }

        Platform platform = null;
        if (request.getPlatformId() != null) {
            platform = platformRepository.findById(request.getPlatformId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Platform", "id", request.getPlatformId()));
        }

        Problem problem = Problem.builder()
                .title(request.getTitle())
                .slug(slug)
                .description(request.getDescription())
                .topic(request.getTopic())
                .difficulty(request.getDifficulty())
                .externalUrl(request.getExternalUrl())
                .platform(platform)
                .isActive(true)
                .createdBy(admin)
                .build();

        Problem saved = problemRepository.save(problem);

        activityLogService.log(admin, "PROBLEM_CREATED", "PROBLEM", saved.getId().toString(),
                Map.of("title", saved.getTitle(), "difficulty", saved.getDifficulty().name()));

        log.info("Problem created: {} ({})", saved.getTitle(), saved.getId());
        return ProblemResponse.from(saved);
    }

    @Transactional
    public ProblemResponse updateProblem(UUID id, UpdateProblemRequest request, User admin) {
        Problem problem = findById(id);

        if (request.getTitle() != null && !request.getTitle().equals(problem.getTitle())) {
            String newSlug = slugUtils.toSlug(request.getTitle());
            if (!newSlug.equals(problem.getSlug()) && problemRepository.existsBySlug(newSlug)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            problem.setTitle(request.getTitle());
            problem.setSlug(newSlug);
        }

        if (request.getDescription() != null) problem.setDescription(request.getDescription());
        if (request.getTopic() != null)       problem.setTopic(request.getTopic());
        if (request.getDifficulty() != null)  problem.setDifficulty(request.getDifficulty());
        if (request.getExternalUrl() != null) problem.setExternalUrl(request.getExternalUrl());
        if (request.getIsActive() != null)    problem.setActive(request.getIsActive());

        if (request.getPlatformId() != null) {
            Platform platform = platformRepository.findById(request.getPlatformId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Platform", "id", request.getPlatformId()));
            problem.setPlatform(platform);
        }

        Problem saved = problemRepository.save(problem);

        activityLogService.log(admin, "PROBLEM_UPDATED", "PROBLEM", saved.getId().toString(),
                Map.of("title", saved.getTitle()));

        log.info("Problem updated: {}", saved.getId());
        return ProblemResponse.from(saved);
    }

    @Transactional
    public void deleteProblem(UUID id, User admin) {
        Problem problem = findById(id);

        activityLogService.log(admin, "PROBLEM_DELETED", "PROBLEM", id.toString(),
                Map.of("title", problem.getTitle()));

        problemRepository.delete(problem);
        log.info("Problem deleted: {} by admin {}", id, admin.getId());
    }

    // ── Helpers ──────────────────────────────────────────────

    public Problem findById(UUID id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "id", id));
    }

    private ProblemResponse enrichWithUserContext(Problem problem, UUID userId) {
        ProblemResponse response = ProblemResponse.from(problem);

        progressRepository.findByUserIdAndProblemId(userId, problem.getId())
                .ifPresent(p -> response.setUserProgressStatus(p.getStatus().name()));

        response.setBookmarked(bookmarkRepository.existsByUserIdAndProblemId(userId, problem.getId()));
        response.setHasNote(noteRepository.existsByUserIdAndProblemId(userId, problem.getId()));

        return response;
    }
}
