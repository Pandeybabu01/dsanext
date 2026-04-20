package com.dsanext.controller;
//
import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.Difficulty;
import com.dsanext.domain.enums.Role;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.CreateProblemRequest;
import com.dsanext.dto.request.UpdateProblemRequest;
import com.dsanext.dto.response.*;
import com.dsanext.service.*;
import com.dsanext.util.PaginationUtils;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Admin Control Center — all endpoints require ADMIN role.
 *
 * ── User Management ──────────────────────────────────────
 * GET    /api/admin/users                      — Search users (paginated)
 * GET    /api/admin/users/{id}                 — Get user by ID
 * PATCH  /api/admin/users/{id}/block           — Block user
 * PATCH  /api/admin/users/{id}/unblock         — Unblock user
 * PATCH  /api/admin/users/{id}/role            — Change user role
 * DELETE /api/admin/users/{id}                 — Delete user
 *
 * ── Problem Management ───────────────────────────────────
 * GET    /api/admin/problems                   — List all problems (paginated, admin view)
 * GET    /api/admin/problems/{id}              — Get problem by ID
 * POST   /api/admin/problems                   — Create problem
 * PUT    /api/admin/problems/{id}              — Update problem
 * DELETE /api/admin/problems/{id}              — Delete problem
 *
 * ── Activity Logs ─────────────────────────────────────────
 * GET    /api/admin/logs                       — Search activity logs (paginated)
 * DELETE /api/admin/logs/purge                 — Purge old logs
 */
@RestController
//@RequestMapping("/admin")
@RequestMapping("/admin")
//@PreAuthorize("hasRole('ADMIN')")
//@RequestMapping("/api/admin")   // ✅ FIXED URL
//@PreAuthorize("hasAuthority('ADMIN')")   // ✅ FIXED ROLE CHECK
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService        userService;
    private final ProblemService     problemService;
    private final ActivityLogService activityLogService;
    private final PaginationUtils    paginationUtils;


    @PostConstruct
    public void init() {
        System.out.println("🔥 ADMIN CONTROLLER LOADED");
    }

    // ── User Management ──────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {

        Pageable pageable = paginationUtils.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(
                userService.searchUsers(search, role, active, pageable)));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserById(id)));
    }

    @PatchMapping("/users/{id}/block")
    public ResponseEntity<ApiResponse<UserResponse>> blockUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {

        UserResponse response = userService.setActiveStatus(id, false, admin);
        return ResponseEntity.ok(ApiResponse.success("User blocked", response));
    }

    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<ApiResponse<UserResponse>> unblockUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {

        UserResponse response = userService.setActiveStatus(id, true, admin);
        return ResponseEntity.ok(ApiResponse.success("User unblocked", response));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable UUID id,
            @RequestParam Role role,
            @AuthenticationPrincipal User admin) {

        UserResponse response = userService.updateRole(id, role, admin);
        return ResponseEntity.ok(ApiResponse.success("User role updated", response));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {

        userService.deleteUser(id, admin);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    // ── Problem Management ───────────────────────────────────

    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<PageResponse<ProblemResponse>>> getProblemsAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {

        Pageable pageable = paginationUtils.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(
                problemService.getProblemsAdmin(search, difficulty, topic, active, pageable)));
    }

    @GetMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(
                problemService.getProblemById(id)));
    }

    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(
            @Valid @RequestBody CreateProblemRequest request,
            @AuthenticationPrincipal User admin) {

        ProblemResponse response = problemService.createProblem(request, admin);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Problem created", response));
    }

    @PutMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> updateProblem(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProblemRequest request,
            @AuthenticationPrincipal User admin) {

        return ResponseEntity.ok(ApiResponse.success("Problem updated",
                problemService.updateProblem(id, request, admin)));
    }

    @DeleteMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {

        problemService.deleteProblem(id, admin);
        return ResponseEntity.ok(ApiResponse.success("Problem deleted"));
    }

    // ── Activity Logs ─────────────────────────────────────────

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PageResponse<ActivityLogResponse>>> searchLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer size) {

        Pageable pageable = paginationUtils.buildPageable(page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(
                activityLogService.searchLogs(userId, action, entityType, from, to, pageable)));
    }

    @DeleteMapping("/logs/purge")
    public ResponseEntity<ApiResponse<java.util.Map<String, Integer>>> purgeLogs(
            @RequestParam(defaultValue = "90") int retentionDays,
            @AuthenticationPrincipal User admin) {

        int deleted = activityLogService.purgeOldLogs(retentionDays);
        return ResponseEntity.ok(ApiResponse.success(
                "Logs purged", java.util.Map.of("deletedCount", deleted)));
    }
}
