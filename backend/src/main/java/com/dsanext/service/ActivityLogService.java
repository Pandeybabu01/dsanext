//package com.dsanext.service;
//
//import com.dsanext.domain.entity.ActivityLog;
//import com.dsanext.domain.entity.User;
//import com.dsanext.dto.response.ActivityLogResponse;
//import com.dsanext.repository.ActivityLogRepository;
//import com.dsanext.dto.common.PageResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Writes activity/audit logs asynchronously so they never slow down
// * the main request thread.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ActivityLogService {
//
//    private final ActivityLogRepository activityLogRepository;
//
//    /**
//     * Persist an activity log entry asynchronously.
//     *
//     * @param user       acting user (may be null for system actions)
//     * @param action     action code e.g. "USER_LOGIN", "PROBLEM_CREATED"
//     * @param entityType entity class name e.g. "USER", "PROBLEM"
//     * @param entityId   string UUID of the affected entity
//     * @param metadata   optional key-value payload (stored as JSONB)
//     */
//    @Async("taskExecutor")
//    @Transactional
//    public void log(User user, String action, String entityType,
//                    String entityId, Map<String, Object> metadata) {
//        try {
//            ActivityLog entry = ActivityLog.builder()
//                    .user(user)
//                    .action(action)
//                    .entityType(entityType)
//                    .entityId(entityId)
//                    .metadata(metadata)
//                    .build();
//            activityLogRepository.save(entry);
//        } catch (Exception ex) {
//            // Log failures must never propagate to the caller
//            log.error("Failed to write activity log [action={}]: {}", action, ex.getMessage());
//        }
//    }
//
//    /**
//     * Overload with IP address and user agent for request-level logging.
//     */
//    @Async("taskExecutor")
//    @Transactional
//    public void log(User user, String action, String entityType, String entityId,
//                    Map<String, Object> metadata, String ipAddress, String userAgent) {
//        try {
//            ActivityLog entry = ActivityLog.builder()
//                    .user(user)
//                    .action(action)
//                    .entityType(entityType)
//                    .entityId(entityId)
//                    .metadata(metadata)
//                    .ipAddress(ipAddress)
//                    .userAgent(userAgent)
//                    .build();
//            activityLogRepository.save(entry);
//        } catch (Exception ex) {
//            log.error("Failed to write activity log [action={}]: {}", action, ex.getMessage());
//        }
//    }
//
//    // ── Admin queries ────────────────────────────────────────
//
//    @Transactional(readOnly = true)
//    public PageResponse<ActivityLogResponse> searchLogs(UUID userId, String action,
//            String entityType, Instant from, Instant to, Pageable pageable) {
//        return PageResponse.from(
//                activityLogRepository.searchLogs(userId, action, entityType, from, to, pageable)
//                        .map(ActivityLogResponse::from)
//        );
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<ActivityLogResponse> getUserLogs(UUID userId, Pageable pageable) {
//        return PageResponse.from(
//                activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
//                        .map(ActivityLogResponse::from)
//        );
//    }
//
//    /**
//     * Purge logs older than retentionDays.
//     * Called by a scheduled task or admin endpoint.
//     */
//    @Transactional
//    public int purgeOldLogs(int retentionDays) {
//        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
//        int deleted = activityLogRepository.deleteOlderThan(cutoff);
//        log.info("Purged {} activity logs older than {} days", deleted, retentionDays);
//        return deleted;
//    }
//}

//package com.dsanext.service;
//
//import com.dsanext.domain.entity.ActivityLog;
//import com.dsanext.domain.entity.User;
//import com.dsanext.dto.response.ActivityLogResponse;
//import com.dsanext.repository.ActivityLogRepository;
//import com.dsanext.dto.common.PageResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Writes activity/audit logs asynchronously so they never slow down
// * the main request thread.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ActivityLogService {
//
//    private final ActivityLogRepository activityLogRepository;
//
//    /**
//     * Persist an activity log entry asynchronously.
//     *
//     * @param user       acting user (may be null for system actions)
//     * @param action     action code e.g. "USER_LOGIN", "PROBLEM_CREATED"
//     * @param entityType entity class name e.g. "USER", "PROBLEM"
//     * @param entityId   string UUID of the affected entity
//     * @param metadata   optional key-value payload (stored as JSONB)
//     */
//    @Async("taskExecutor")
//    @Transactional
//    public void log(User user, String action, String entityType,
//                    String entityId, Map<String, Object> metadata) {
//        try {
//            ActivityLog entry = ActivityLog.builder()
//                    .user(user)
//                    .action(action)
//                    .entityType(entityType)
//                    .entityId(entityId)
//                    .metadata(metadata)
//                    .build();
//            activityLogRepository.save(entry);
//        } catch (Exception ex) {
//            // Log failures must never propagate to the caller
//            log.error("Failed to write activity log [action={}]: {}", action, ex.getMessage());
//        }
//    }
//
//    /**
//     * Overload with IP address and user agent for request-level logging.
//     */
//    @Async("taskExecutor")
//    @Transactional
//    public void log(User user, String action, String entityType, String entityId,
//                    Map<String, Object> metadata, String ipAddress, String userAgent) {
//        try {
//            ActivityLog entry = ActivityLog.builder()
//                    .user(user)
//                    .action(action)
//                    .entityType(entityType)
//                    .entityId(entityId)
//                    .metadata(metadata)
//                    .ipAddress(ipAddress)
//                    .userAgent(userAgent)
//                    .build();
//            activityLogRepository.save(entry);
//        } catch (Exception ex) {
//            log.error("Failed to write activity log [action={}]: {}", action, ex.getMessage());
//        }
//    }
//
//    // ── Admin queries ────────────────────────────────────────
//
//    @Transactional(readOnly = true)
//    public PageResponse<ActivityLogResponse> searchLogs(UUID userId, String action,
//                                                        String entityType, Instant from, Instant to, Pageable pageable) {
//        try {
//            return PageResponse.from(
//                    activityLogRepository.searchLogs(
//                                    userId,
//                                    (action != null && action.isBlank()) ? null : action,
//                                    (entityType != null && entityType.isBlank()) ? null : entityType,
//                                    from, to, pageable)
//                            .map(ActivityLogResponse::from)
//            );
//        } catch (Exception ex) {
//            log.error("Failed to search activity logs: {}", ex.getMessage(), ex);
//            throw ex;
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<ActivityLogResponse> getUserLogs(UUID userId, Pageable pageable) {
//        return PageResponse.from(
//                activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
//                        .map(ActivityLogResponse::from)
//        );
//    }
//
//    /**
//     * Purge logs older than retentionDays.
//     * Called by a scheduled task or admin endpoint.
//     */
//    @Transactional
//    public int purgeOldLogs(int retentionDays) {
//        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
//        int deleted = activityLogRepository.deleteOlderThan(cutoff);
//        log.info("Purged {} activity logs older than {} days", deleted, retentionDays);
//        return deleted;
//    }
//}

//package com.dsanext.service;
//
//import com.dsanext.domain.entity.ActivityLog;
//import com.dsanext.domain.entity.User;
//import com.dsanext.dto.response.ActivityLogResponse;
//import com.dsanext.repository.ActivityLogRepository;
//import com.dsanext.dto.common.PageResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.Map;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ActivityLogService {
//
//    private final ActivityLogRepository activityLogRepository;
//
//    @Async("taskExecutor")
//    @Transactional
//    public void log(User user, String action, String entityType,
//                    String entityId, Map<String, Object> metadata) {
//        try {
//            ActivityLog entry = ActivityLog.builder()
//                    .userId(user != null ? user.getId() : null) // ✅ FIX
//                    .action(action)
//                    .entityType(entityType)
//                    .entityId(entityId)
//                    .metadata(metadata)
//                    .build();
//
//            activityLogRepository.save(entry);
//        } catch (Exception ex) {
//            log.error("Failed to write activity log [action={}]: {}", action, ex.getMessage());
//        }
//    }
//
//    @Async("taskExecutor")
//    @Transactional
//    public void log(User user, String action, String entityType, String entityId,
//                    Map<String, Object> metadata, String ipAddress, String userAgent) {
//        try {
//            ActivityLog entry = ActivityLog.builder()
//                    .userId(user != null ? user.getId() : null) // ✅ FIX
//                    .action(action)
//                    .entityType(entityType)
//                    .entityId(entityId)
//                    .metadata(metadata)
//                    .ipAddress(ipAddress)
//                    .userAgent(userAgent)
//                    .build();
//
//            activityLogRepository.save(entry);
//        } catch (Exception ex) {
//            log.error("Failed to write activity log [action={}]: {}", action, ex.getMessage());
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<ActivityLogResponse> searchLogs(UUID userId, String action,
//                                                        String entityType, Instant from, Instant to, Pageable pageable) {
//        return PageResponse.from(
//                activityLogRepository.searchLogs(
//                        userId,
//                        (action != null && action.isBlank()) ? null : action,
//                        (entityType != null && entityType.isBlank()) ? null : entityType,
//                        from, to, pageable
//                ).map(ActivityLogResponse::from)
//        );
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<ActivityLogResponse> getUserLogs(UUID userId, Pageable pageable) {
//        return PageResponse.from(
//                activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
//                        .map(ActivityLogResponse::from)
//        );
//    }
//
//    @Transactional
//    public int purgeOldLogs(int retentionDays) {
//        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
//        int deleted = activityLogRepository.deleteOlderThan(cutoff);
//        log.info("Purged {} activity logs older than {} days", deleted, retentionDays);
//        return deleted;
//    }
//}
package com.dsanext.service;

import com.dsanext.domain.entity.ActivityLog;
import com.dsanext.domain.entity.User;
import com.dsanext.dto.response.ActivityLogResponse;
import com.dsanext.repository.ActivityLogRepository;
import com.dsanext.dto.common.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper          objectMapper;

    // ── Helpers ──────────────────────────────────────────────

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null) return null;
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // ── Async log writes ──────────────────────────────────────

    @Async("taskExecutor")
    @Transactional
    public void log(User user, String action, String entityType,
                    String entityId, Map<String, Object> metadata) {
        try {
            ActivityLog entry = ActivityLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .metadata(toJson(metadata))
                    .build();
            activityLogRepository.save(entry);
        } catch (Exception ex) {
            log.error("Failed to write activity log [action={}]: {}", action, ex.getMessage());
        }
    }

    @Async("taskExecutor")
    @Transactional
    public void log(User user, String action, String entityType, String entityId,
                    Map<String, Object> metadata, String ipAddress, String userAgent) {
        try {
            ActivityLog entry = ActivityLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .metadata(toJson(metadata))
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            activityLogRepository.save(entry);
        } catch (Exception ex) {
            log.error("Failed to write activity log [action={}]: {}", action, ex.getMessage());
        }
    }

    // ── Admin queries ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> searchLogs(UUID userId, String action,
                                                        String entityType, Instant from, Instant to, Pageable pageable) {
        try {
            String actionParam     = (action     != null && action.isBlank())     ? null : action;
            String entityTypeParam = (entityType != null && entityType.isBlank()) ? null : entityType;
            String fromStr         = (from != null) ? from.toString() : null;
            String toStr           = (to   != null) ? to.toString()   : null;

            // Use PageRequest WITHOUT sort — ORDER BY is hardcoded in native SQL
            // to prevent Hibernate from translating Java field names (createdAt → l.createdat)
            Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

            return PageResponse.from(
                    activityLogRepository.searchLogsNative(
                                    actionParam, entityTypeParam, fromStr, toStr, unsorted)
                            .map(ActivityLogResponse::from)
            );
        } catch (Exception ex) {
            log.error("Failed to search activity logs: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> getUserLogs(UUID userId, Pageable pageable) {
        return PageResponse.from(
                activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                        .map(ActivityLogResponse::from)
        );
    }

    @Transactional
    public int purgeOldLogs(int retentionDays) {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = activityLogRepository.deleteOlderThan(cutoff);
        log.info("Purged {} activity logs older than {} days", deleted, retentionDays);
        return deleted;
    }
}