package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.PlatformRequest;
import com.dsanext.dto.response.PlatformResponse;
import com.dsanext.service.PlatformService;
import com.dsanext.util.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Platform endpoints.
 *
 * GET    /api/platforms            — List all active platforms (public)
 * GET    /api/platforms/admin      — Admin paginated search (ADMIN)
 * GET    /api/platforms/{id}       — Get platform by ID (public)
 * POST   /api/platforms            — Create platform (ADMIN)
 * PUT    /api/platforms/{id}       — Update platform (ADMIN)
 * DELETE /api/platforms/{id}       — Delete platform (ADMIN)
 */
@RestController
@RequestMapping("/platforms")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformService platformService;
    private final PaginationUtils paginationUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlatformResponse>>> getAllActivePlatforms() {
        return ResponseEntity.ok(ApiResponse.success(
                platformService.getAllActivePlatforms()));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PlatformResponse>>> searchPlatformsAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        Pageable pageable = paginationUtils.buildPageable(page, size, "name", "asc");
        return ResponseEntity.ok(ApiResponse.success(
                platformService.searchPlatforms(search, active, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlatformResponse>> getPlatformById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(
                platformService.getPlatformById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlatformResponse>> createPlatform(
            @Valid @RequestBody PlatformRequest request,
            @AuthenticationPrincipal User admin) {

        PlatformResponse response = platformService.createPlatform(request, admin);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Platform created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlatformResponse>> updatePlatform(
            @PathVariable UUID id,
            @Valid @RequestBody PlatformRequest request,
            @AuthenticationPrincipal User admin) {

        return ResponseEntity.ok(ApiResponse.success("Platform updated",
                platformService.updatePlatform(id, request, admin)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlatform(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {

        platformService.deletePlatform(id, admin);
        return ResponseEntity.ok(ApiResponse.success("Platform deleted"));
    }
}
