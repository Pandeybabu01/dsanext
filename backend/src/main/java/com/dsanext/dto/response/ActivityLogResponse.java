//package com.dsanext.dto.response;
//
//import com.dsanext.domain.entity.ActivityLog;
//import lombok.Builder;
//import lombok.Data;
//
//import java.time.Instant;
//import java.util.Map;
//import java.util.UUID;
//
//@Data
//@Builder
//public class ActivityLogResponse {
//
//    private UUID id;
//    private UUID userId;
//    private String userEmail;
//    private String action;
//    private String entityType;
//    private String entityId;
//    private Map<String, Object> metadata;
//    private String ipAddress;
//    private Instant createdAt;
//
//    public static ActivityLogResponse from(ActivityLog log) {
//        return ActivityLogResponse.builder()
//                .id(log.getId())
//                .userId(log.getUser() != null ? log.getUser().getId() : null)
//                .userEmail(log.getUser() != null ? log.getUser().getEmail() : "system")
//                .action(log.getAction())
//                .entityType(log.getEntityType())
//                .entityId(log.getEntityId())
//                .metadata(log.getMetadata())
//                .ipAddress(log.getIpAddress())
//                .createdAt(log.getCreatedAt())
//                .build();
//    }
//}

//package com.dsanext.dto.response;
//
//import com.dsanext.domain.entity.ActivityLog;
//import lombok.Builder;
//import lombok.Data;
//
//import java.time.Instant;
//import java.util.Collections;
//import java.util.Map;
//import java.util.UUID;
//
//@Data
//@Builder
//public class ActivityLogResponse {
//
//    private UUID id;
//    private UUID userId;
//    private String userEmail;
//    private String action;
//    private String entityType;
//    private String entityId;
//    private Map<String, Object> metadata;
//    private String ipAddress;
//    private Instant createdAt;
//
//    public static ActivityLogResponse from(ActivityLog log) {
//
//        if (log == null) return null;
//
//        return ActivityLogResponse.builder()
//                .id(log.getId())
//
//                .userId(
//                        log.getUser() != null ? log.getUser().getId() : null
//                )
//
//                .userEmail(
//                        log.getUser() != null
//                                ? log.getUser().getEmail()
//                                : "system"
//                )
//
//                .action(
//                        log.getAction() != null ? log.getAction() : "UNKNOWN"
//                )
//
//                .entityType(log.getEntityType())
//
//                .entityId(log.getEntityId())
//
//                // ✅ SAFE FIX (prevents null crash)
//                .metadata(
//                        log.getMetadata() != null
//                                ? log.getMetadata()
//                                : Collections.emptyMap()
//                )
//
//                .ipAddress(log.getIpAddress())
//
//                // safe timestamp (no transformation, just null-safe object)
//                .createdAt(log.getCreatedAt())
//
//                .build();
//    }
//}



package com.dsanext.dto.response;

import com.dsanext.domain.entity.ActivityLog;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ActivityLogResponse {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String action;
    private String entityType;
    private String entityId;
    private String metadata;
    private String ipAddress;
    private Instant createdAt;

    public static ActivityLogResponse from(ActivityLog log) {

        UUID userId = null;
        String email = "system";

        // ✅ Safe access (prevents LazyInitialization + null issues)
        if (log.getUser() != null) {
            try {
                userId = log.getUser().getId();
                email = log.getUser().getEmail();
            } catch (Exception ignored) {
                // avoids Hibernate lazy loading crash
            }
        }

        return ActivityLogResponse.builder()
                .id(log.getId())
                .userId(userId)
                .userEmail(email)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .metadata(log.getMetadata()) // already TEXT, no parsing
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}