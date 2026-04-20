package com.dsanext.repository;

import com.dsanext.domain.entity.Progress;
import com.dsanext.domain.enums.Difficulty;
import com.dsanext.domain.enums.ProgressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, UUID> {

    Optional<Progress> findByUserIdAndProblemId(UUID userId, UUID problemId);

    boolean existsByUserIdAndProblemId(UUID userId, UUID problemId);

    long countByUserIdAndStatus(UUID userId, ProgressStatus status);

    long countByUserId(UUID userId);

    /**
     * All progress entries for a user, with problem and platform eagerly loaded.
     */
    @Query("""
        SELECT pr FROM Progress pr
        JOIN FETCH pr.problem p
        LEFT JOIN FETCH p.platform pl
        WHERE pr.user.id = :userId
          AND (:status IS NULL OR pr.status = :status)
        """)
    Page<Progress> findByUserIdFiltered(
            @Param("userId") UUID userId,
            @Param("status") ProgressStatus status,
            Pageable pageable);

    /**
     * Progress summary by difficulty for a user — for analytics dashboard.
     * Returns [difficulty, status, count].
     */
    @Query("""
        SELECT p.difficulty, pr.status, COUNT(pr)
        FROM Progress pr
        JOIN pr.problem p
        WHERE pr.user.id = :userId
        GROUP BY p.difficulty, pr.status
        """)
    List<Object[]> getProgressByDifficulty(@Param("userId") UUID userId);

    /**
     * Progress summary by topic for a user — for analytics dashboard.
     * Returns [topic, solvedCount, totalCount].
     */
    @Query("""
        SELECT p.topic,
               SUM(CASE WHEN pr.status = 'SOLVED' THEN 1 ELSE 0 END),
               COUNT(pr)
        FROM Progress pr
        JOIN pr.problem p
        WHERE pr.user.id = :userId
        GROUP BY p.topic
        ORDER BY COUNT(pr) DESC
        """)
    List<Object[]> getProgressByTopic(@Param("userId") UUID userId);

    /**
     * Recent activity — last N solved problems for a user.
     */
    @Query("""
        SELECT pr FROM Progress pr
        JOIN FETCH pr.problem p
        WHERE pr.user.id = :userId AND pr.status = 'SOLVED'
        ORDER BY pr.solvedAt DESC
        """)
    List<Progress> findRecentSolved(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Global platform analytics — total problems solved per day (last 30 days).
     */
    @Query(value = """
        SELECT DATE(solved_at) as solve_date, COUNT(*) as count
        FROM progress
        WHERE status = 'SOLVED'
          AND solved_at >= NOW() - INTERVAL '30 days'
        GROUP BY DATE(solved_at)
        ORDER BY solve_date
        """, nativeQuery = true)
    List<Object[]> getDailySolvedStats();
}
