package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.ProgressStatus;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.UpdateProgressRequest;
import com.dsanext.dto.response.ProgressResponse;
import com.dsanext.service.ProgressService;
import com.dsanext.util.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User progress tracking endpoints — all require JWT.
 *
 * GET  /api/progress              — List own progress (paginated, filterable by status)
 * GET  /api/progress/{problemId}  — Get progress for a specific problem
 * PUT  /api/progress/{problemId}  — Create or update progress for a problem
 */
@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final PaginationUtils paginationUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProgressResponse>>> getMyProgress(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) ProgressStatus status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "updatedAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {

        Pageable pageable = paginationUtils.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(
                progressService.getUserProgress(user.getId(), status, pageable)));
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<ProgressResponse>> getProgressForProblem(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId) {

        return ResponseEntity.ok(ApiResponse.success(
                progressService.getProgressForProblem(user.getId(), problemId)));
    }

    @PutMapping("/{problemId}")
    public ResponseEntity<ApiResponse<ProgressResponse>> upsertProgress(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId,
            @Valid @RequestBody UpdateProgressRequest request) {

        ProgressResponse response = progressService.upsertProgress(
                user.getId(), problemId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Progress updated", response));
    }
}
