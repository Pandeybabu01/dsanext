//package com.dsanext.domain.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.type.SqlTypes;
//
//import java.time.Instant;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Immutable audit log entry.
// * Records every significant user and admin action with optional JSONB metadata.
// */
//@Entity
//@Table(name = "activity_logs")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ActivityLog {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(nullable = false, updatable = false)
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @Column(nullable = false, length = 100)
//    private String action;
//
//    @Column(name = "entity_type", length = 100)
//    private String entityType;
//
//    @Column(name = "entity_id", length = 36)
//    private String entityId;
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "metadata", columnDefinition = "jsonb")
//    private Map<String, Object> metadata;
//
//    @Column(name = "ip_address", length = 45)
//    private String ipAddress;
//
//    @Column(name = "user_agent", length = 500)
//    private String userAgent;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    @Builder.Default
//    private Instant createdAt = Instant.now();
//}


//package com.dsanext.domain.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.type.SqlTypes;
//
//import java.time.Instant;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Immutable audit log entry.
// * Records every significant user and admin action with optional JSONB metadata.
// */
//@Entity
//@Table(name = "activity_logs")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ActivityLog {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(nullable = false, updatable = false)
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @Column(nullable = false, length = 100)
//    private String action;
//
//    @Column(name = "entity_type", length = 100)
//    private String entityType;
//
//    @Column(name = "entity_id", length = 36)
//    private String entityId;
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "metadata", columnDefinition = "jsonb")
//    private Map<String, Object> metadata;
//
//    @Column(name = "ip_address", length = 45)
//    private String ipAddress;
//
//    @Column(name = "user_agent", length = 500)
//    private String userAgent;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    @Builder.Default
//    private Instant createdAt = Instant.now();
//}


//package com.dsanext.domain.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.type.SqlTypes;
//
//import java.time.Instant;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Audit log entry (production-safe version)
// * - Removed Hibernate lazy dependency risk
// * - Uses userId instead of User entity relation
// * - Fully safe for logging & high traffic systems
// */
//
//@Entity
//@Table(name = "activity_logs")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ActivityLog {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(nullable = false, updatable = false)
//    private UUID id;
//
//    // ✅ FIX: replaced User relation with simple UUID to avoid lazy loading crashes
//    @Column(name = "user_id")
//    private UUID userId;
//
//    @Column(nullable = false, length = 100)
//    private String action;
//
//    @Column(name = "entity_type", length = 100)
//    private String entityType;
//
//    @Column(name = "entity_id", length = 36)
//    private String entityId;
//
//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "metadata", columnDefinition = "jsonb")
//    private Map<String, Object> metadata;
//
//    @Column(name = "ip_address", length = 45)
//    private String ipAddress;
//
//    @Column(name = "user_agent", length = 500)
//    private String userAgent;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    @Builder.Default
//    private Instant createdAt = Instant.now();
//}

package com.dsanext.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit log entry.
 * metadata stored as TEXT (JSON string) to avoid Hibernate 6 JSONB issues.
 */
@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 36)
    private String entityId;

    // Stored as text to avoid Hibernate 6 JSONB mapping issues
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}