package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.request.UpdateAppSettingRequest;
import com.dsanext.dto.request.UserSettingRequest;
import com.dsanext.dto.response.AppSettingResponse;
import com.dsanext.dto.response.UserSettingResponse;
import com.dsanext.service.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Settings endpoints.
 *
 * GET  /api/settings/public              — Public app settings map (unauthenticated)
 * GET  /api/settings/me                  — Get own user settings (JWT)
 * PUT  /api/settings/me                  — Update own user settings (JWT)
 * GET  /api/settings/admin               — All app settings (ADMIN)
 * GET  /api/settings/admin/{key}         — Single app setting (ADMIN)
 * PUT  /api/settings/admin/{key}         — Update app setting (ADMIN)
 */
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    // ── Public ───────────────────────────────────────────────

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicSettings() {
        return ResponseEntity.ok(ApiResponse.success(
                settingService.getPublicSettingsMap()));
    }

    // ── User ─────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSettingResponse>> getMySettings(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success(
                settingService.getUserSettings(user.getId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserSettingResponse>> updateMySettings(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserSettingRequest request) {

        UserSettingResponse response = settingService.updateUserSettings(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated", response));
    }

    // ── Admin ─────────────────────────────────────────────────

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AppSettingResponse>>> getAllAppSettings() {
        return ResponseEntity.ok(ApiResponse.success(
                settingService.getAllAppSettings()));
    }

    @GetMapping("/admin/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppSettingResponse>> getAppSetting(
            @PathVariable String key) {

        return ResponseEntity.ok(ApiResponse.success(
                settingService.getAppSetting(key)));
    }

    @PutMapping("/admin/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppSettingResponse>> updateAppSetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateAppSettingRequest request,
            @AuthenticationPrincipal User admin) {

        AppSettingResponse response = settingService.updateAppSetting(key, request, admin);
        return ResponseEntity.ok(ApiResponse.success("Setting updated", response));
    }
}
