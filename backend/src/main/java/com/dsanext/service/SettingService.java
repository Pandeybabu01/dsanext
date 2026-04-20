package com.dsanext.service;

import com.dsanext.domain.entity.AppSetting;
import com.dsanext.domain.entity.User;
import com.dsanext.domain.entity.UserSetting;
import com.dsanext.dto.request.UpdateAppSettingRequest;
import com.dsanext.dto.request.UserSettingRequest;
import com.dsanext.dto.response.AppSettingResponse;
import com.dsanext.dto.response.UserSettingResponse;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.repository.AppSettingRepository;
import com.dsanext.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingService {

    private final UserSettingRepository userSettingRepository;
    private final AppSettingRepository  appSettingRepository;
    private final ActivityLogService    activityLogService;

    // ── User settings ────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserSettingResponse getUserSettings(UUID userId) {
        UserSetting settings = userSettingRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSetting", "userId", userId));
        return UserSettingResponse.from(settings);
    }

    @Transactional
    public UserSettingResponse updateUserSettings(UUID userId, UserSettingRequest request) {
        UserSetting settings = userSettingRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSetting", "userId", userId));

        if (request.getTheme() != null)                settings.setTheme(request.getTheme());
        if (request.getNotificationsEnabled() != null) settings.setNotificationsEnabled(request.getNotificationsEnabled());
        if (request.getEmailNotifications() != null)   settings.setEmailNotifications(request.getEmailNotifications());
        if (request.getLcUsername() != null)           settings.setLcUsername(request.getLcUsername());
        if (request.getCfUsername() != null)           settings.setCfUsername(request.getCfUsername());
        if (request.getHrUsername() != null)           settings.setHrUsername(request.getHrUsername());
        if (request.getIbUsername() != null)           settings.setIbUsername(request.getIbUsername());

        return UserSettingResponse.from(userSettingRepository.save(settings));
    }

    // ── App settings (admin) ─────────────────────────────────

    @Transactional(readOnly = true)
    public List<AppSettingResponse> getAllAppSettings() {
        return appSettingRepository.findAll().stream()
                .map(AppSettingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppSettingResponse> getPublicAppSettings() {
        return appSettingRepository.findAllByIsPublicTrue().stream()
                .map(AppSettingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, String> getPublicSettingsMap() {
        return appSettingRepository.findAllByIsPublicTrue().stream()
                .collect(Collectors.toMap(AppSetting::getSettingKey, AppSetting::getSettingValue));
    }

    @Transactional(readOnly = true)
    public AppSettingResponse getAppSetting(String key) {
        AppSetting setting = appSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("AppSetting", "key", key));
        return AppSettingResponse.from(setting);
    }

    @Transactional
    public AppSettingResponse updateAppSetting(String key,
            UpdateAppSettingRequest request, User admin) {
        AppSetting setting = appSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("AppSetting", "key", key));

        String oldValue = setting.getSettingValue();
        setting.setSettingValue(request.getValue());
        setting.setUpdatedBy(admin);

        AppSetting saved = appSettingRepository.save(setting);

        activityLogService.log(admin, "APP_SETTING_UPDATED", "SETTING",
                saved.getId().toString(),
                Map.of("key", key, "oldValue", oldValue, "newValue", request.getValue()));

        log.info("App setting '{}' updated by admin {}", key, admin.getId());
        return AppSettingResponse.from(saved);
    }

    // ── Feature flag helpers ─────────────────────────────────

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(String featureKey) {
        return appSettingRepository.findBySettingKey(featureKey)
                .map(AppSetting::getBooleanValue)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isMaintenanceMode() {
        return isFeatureEnabled("app.maintenance_mode");
    }

    @Transactional(readOnly = true)
    public String getAppName() {
        return appSettingRepository.findBySettingKey("app.name")
                .map(AppSetting::getSettingValue)
                .orElse("DSANext");
    }
}
