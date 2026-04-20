package com.dsanext.service;

import com.dsanext.domain.entity.Platform;
import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.PlatformRequest;
import com.dsanext.dto.response.PlatformResponse;
import com.dsanext.exception.DuplicateResourceException;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.repository.PlatformRepository;
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
public class PlatformService {

    private final PlatformRepository platformRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PlatformResponse> getAllActivePlatforms() {
        return platformRepository.findAllByIsActiveOrderByNameAsc(true)
                .stream().map(PlatformResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<PlatformResponse> searchPlatforms(String search,
            Boolean active, Pageable pageable) {
        return PageResponse.from(
                platformRepository.searchPlatforms(search, active, pageable)
                        .map(PlatformResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public PlatformResponse getPlatformById(UUID id) {
        return PlatformResponse.from(findById(id));
    }

    @Transactional
    public PlatformResponse createPlatform(PlatformRequest request, User admin) {
        if (platformRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Platform", "name", request.getName());
        }

        Platform platform = Platform.builder()
                .name(request.getName())
                .baseUrl(request.getBaseUrl())
                .iconUrl(request.getIconUrl())
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .build();

        Platform saved = platformRepository.save(platform);

        activityLogService.log(admin, "PLATFORM_CREATED", "PLATFORM",
                saved.getId().toString(), Map.of("name", saved.getName()));

        log.info("Platform created: {} by admin {}", saved.getName(), admin.getId());
        return PlatformResponse.from(saved);
    }

    @Transactional
    public PlatformResponse updatePlatform(UUID id, PlatformRequest request, User admin) {
        Platform platform = findById(id);

        if (!platform.getName().equalsIgnoreCase(request.getName())
                && platformRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Platform", "name", request.getName());
        }

        platform.setName(request.getName());
        platform.setBaseUrl(request.getBaseUrl());
        if (request.getIconUrl() != null) platform.setIconUrl(request.getIconUrl());
        if (request.getIsActive() != null) platform.setActive(request.getIsActive());

        Platform saved = platformRepository.save(platform);

        activityLogService.log(admin, "PLATFORM_UPDATED", "PLATFORM",
                saved.getId().toString(), Map.of("name", saved.getName()));

        return PlatformResponse.from(saved);
    }

    @Transactional
    public void deletePlatform(UUID id, User admin) {
        Platform platform = findById(id);

        activityLogService.log(admin, "PLATFORM_DELETED", "PLATFORM",
                id.toString(), Map.of("name", platform.getName()));

        platformRepository.delete(platform);
        log.info("Platform {} deleted by admin {}", id, admin.getId());
    }

    public Platform findById(UUID id) {
        return platformRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Platform", "id", id));
    }
}
