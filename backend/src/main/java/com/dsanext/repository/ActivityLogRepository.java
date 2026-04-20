//package com.dsanext.repository;
//
//import com.dsanext.domain.entity.ActivityLog;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//@Repository
//public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
//
//    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
//
//    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
//
//    /**
//     * Admin log search — filter by user, action, entity type, date range.
//     */
//    @Query("""
//        SELECT l FROM ActivityLog l
//        LEFT JOIN l.user u
//        WHERE (:userId IS NULL OR l.user.id = :userId)
//          AND (:action IS NULL OR :action = ''
//               OR LOWER(l.action) LIKE LOWER(CONCAT('%', :action, '%')))
//          AND (:entityType IS NULL OR :entityType = ''
//               OR l.entityType = :entityType)
//          AND (:from IS NULL OR l.createdAt >= :from)
//          AND (:to   IS NULL OR l.createdAt <= :to)
//        ORDER BY l.createdAt DESC
//        """)
//    Page<ActivityLog> searchLogs(
//            @Param("userId")     UUID userId,
//            @Param("action")     String action,
//            @Param("entityType") String entityType,
//            @Param("from")       Instant from,
//            @Param("to")         Instant to,
//            Pageable pageable);
//
//    /**
//     * Recent activity for a user — last N log entries.
//     */
//    @Query("""
//        SELECT l FROM ActivityLog l
//        WHERE l.user.id = :userId
//        ORDER BY l.createdAt DESC
//        """)
//    List<ActivityLog> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);
//
//    /**
//     * Purge logs older than a given timestamp (used by retention policy).
//     */
//    @Modifying
//    @Query("DELETE FROM ActivityLog l WHERE l.createdAt < :before")
//    int deleteOlderThan(@Param("before") Instant before);
//
//    /**
//     * Count logs per action type — for admin analytics.
//     */
//    @Query("""
//        SELECT l.action, COUNT(l)
//        FROM ActivityLog l
//        WHERE l.createdAt >= :since
//        GROUP BY l.action
//        ORDER BY COUNT(l) DESC
//        """)
//    List<Object[]> countByActionSince(@Param("since") Instant since);
//}



//package com.dsanext.repository;
//
//import com.dsanext.domain.entity.ActivityLog;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//@Repository
//public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
//
//    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
//
//    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
//
//    /**
//     * Admin log search — filter by user, action, entity type, date range.
//     */
//    @Query("""
//        SELECT l FROM ActivityLog l
//        WHERE (:userId IS NULL OR l.user.id = :userId)
//          AND (:action IS NULL OR :action = ''
//               OR LOWER(l.action) LIKE LOWER(CONCAT('%', :action, '%')))
//          AND (:entityType IS NULL OR :entityType = ''
//               OR l.entityType = :entityType)
//          AND (:from IS NULL OR l.createdAt >= :from)
//          AND (:to   IS NULL OR l.createdAt <= :to)
//        ORDER BY l.createdAt DESC
//        """)
//    Page<ActivityLog> searchLogs(
//            @Param("userId")     UUID userId,
//            @Param("action")     String action,
//            @Param("entityType") String entityType,
//            @Param("from")       Instant from,
//            @Param("to")         Instant to,
//            Pageable pageable);
//
//    /**
//     * Recent activity for a user — last N log entries.
//     */
//    @Query("""
//        SELECT l FROM ActivityLog l
//        WHERE l.user.id = :userId
//        ORDER BY l.createdAt DESC
//        """)
//    List<ActivityLog> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);
//
//    /**
//     * Purge logs older than a given timestamp (used by retention policy).
//     */
//    @Modifying
//    @Query("DELETE FROM ActivityLog l WHERE l.createdAt < :before")
//    int deleteOlderThan(@Param("before") Instant before);
//
//    /**
//     * Count logs per action type — for admin analytics.
//     */
//    @Query("""
//        SELECT l.action, COUNT(l)
//        FROM ActivityLog l
//        WHERE l.createdAt >= :since
//        GROUP BY l.action
//        ORDER BY COUNT(l) DESC
//        """)
//    List<Object[]> countByActionSince(@Param("since") Instant since);
//}

package com.dsanext.repository;

import com.dsanext.domain.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Native SQL search — avoids Hibernate 6 JPQL nullable association join issues.
     * ORDER BY is hardcoded in SQL (not via Pageable.sort) to prevent Hibernate
     * from translating Java field names (createdAt) into the SQL ORDER BY clause.
     */
    @Query(value = """
        SELECT l.* FROM activity_logs l
        WHERE (:action IS NULL OR LOWER(l.action) LIKE LOWER(CONCAT('%', :action, '%')))
          AND (:entityType IS NULL OR l.entity_type = :entityType)
          AND (:fromDate IS NULL OR l.created_at >= CAST(:fromDate AS TIMESTAMPTZ))
          AND (:toDate   IS NULL OR l.created_at <= CAST(:toDate   AS TIMESTAMPTZ))
        ORDER BY l.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM activity_logs l
        WHERE (:action IS NULL OR LOWER(l.action) LIKE LOWER(CONCAT('%', :action, '%')))
          AND (:entityType IS NULL OR l.entity_type = :entityType)
          AND (:fromDate IS NULL OR l.created_at >= CAST(:fromDate AS TIMESTAMPTZ))
          AND (:toDate   IS NULL OR l.created_at <= CAST(:toDate   AS TIMESTAMPTZ))
        """,
            nativeQuery = true)
    Page<ActivityLog> searchLogsNative(
            @Param("action")     String action,
            @Param("entityType") String entityType,
            @Param("fromDate")   String fromDate,
            @Param("toDate")     String toDate,
            Pageable pageable);

    /**
     * Purge logs older than a given timestamp.
     */
    @Modifying
    @Query("DELETE FROM ActivityLog l WHERE l.createdAt < :before")
    int deleteOlderThan(@Param("before") Instant before);

    /**
     * Count logs per action type.
     */
    @Query("""
        SELECT l.action, COUNT(l)
        FROM ActivityLog l
        WHERE l.createdAt >= :since
        GROUP BY l.action
        ORDER BY COUNT(l) DESC
        """)
    List<Object[]> countByActionSince(@Param("since") Instant since);
}