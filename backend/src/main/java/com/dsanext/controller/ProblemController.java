package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.Difficulty;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.response.ProblemResponse;
import com.dsanext.service.ProblemService;
import com.dsanext.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Problem listing and detail endpoints.
 *
 * GET /api/problems              — List problems (public, paginated, filterable)
 * GET /api/problems/topics       — Get all distinct topics (public)
 * GET /api/problems/{slug}       — Get problem by slug (public)
 * GET /api/problems/{slug}/detail — Get problem with user context (requires JWT)
 */
@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService  problemService;
    private final PaginationUtils paginationUtils;

    /**
     * Public problem list — filterable by search, difficulty, topic, platform.
     * When a JWT is present, enriches each problem with user progress/bookmark/note flags.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProblemResponse>>> getProblems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) UUID platformId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @AuthenticationPrincipal User user) {

        Pageable pageable = paginationUtils.buildPageable(page, size, sortBy, direction);

        PageResponse<ProblemResponse> result = (user != null)
                ? problemService.getProblemsForUser(search, difficulty, topic, platformId,
                        user.getId(), pageable)
                : problemService.getProblems(search, difficulty, topic, platformId, pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getDistinctTopics() {
        return ResponseEntity.ok(
                ApiResponse.success(problemService.getDistinctTopics()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal User user) {

        ProblemResponse result = (user != null)
                ? problemService.getProblemBySlugForUser(slug, user.getId())
                : problemService.getProblemBySlug(slug);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
