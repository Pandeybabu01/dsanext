package com.dsanext.repository;

import com.dsanext.domain.entity.Problem;
import com.dsanext.domain.enums.Difficulty;
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
public interface ProblemRepository extends JpaRepository<Problem, UUID> {

    Optional<Problem> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByTitleIgnoreCase(String title);

    long countByIsActive(boolean isActive);

    long countByDifficulty(Difficulty difficulty);

    /**
     * Filtered problem list — supports search, difficulty, topic, platform filters.
     * Returns only active problems. Used by the public problem list page.
     */
    @Query("""
        SELECT p FROM Problem p
        LEFT JOIN FETCH p.platform pl
        WHERE p.isActive = true
          AND (:search IS NULL OR :search = ''
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.topic) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:difficulty IS NULL OR p.difficulty = :difficulty)
          AND (:topic IS NULL OR :topic = '' OR LOWER(p.topic) = LOWER(:topic))
          AND (:platformId IS NULL OR pl.id = :platformId)
        """)
    Page<Problem> findAllFiltered(
            @Param("search")     String search,
            @Param("difficulty") Difficulty difficulty,
            @Param("topic")      String topic,
            @Param("platformId") UUID platformId,
            Pageable pageable);

    /**
     * Admin problem list — includes inactive problems, all filters.
     */
    @Query("""
        SELECT p FROM Problem p
        LEFT JOIN FETCH p.platform pl
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:difficulty IS NULL OR p.difficulty = :difficulty)
          AND (:topic IS NULL OR :topic = '' OR LOWER(p.topic) = LOWER(:topic))
          AND (:active IS NULL OR p.isActive = :active)
        """)
    Page<Problem> findAllAdmin(
            @Param("search")     String search,
            @Param("difficulty") Difficulty difficulty,
            @Param("topic")      String topic,
            @Param("active")     Boolean active,
            Pageable pageable);

    /**
     * Distinct list of all topics in the active problem set.
     * Used to populate filter dropdowns.
     */
    @Query("SELECT DISTINCT p.topic FROM Problem p WHERE p.isActive = true ORDER BY p.topic")
    List<String> findDistinctTopics();

    /**
     * Count problems grouped by difficulty — for analytics.
     */
    @Query("""
        SELECT p.difficulty, COUNT(p)
        FROM Problem p WHERE p.isActive = true
        GROUP BY p.difficulty
        """)
    List<Object[]> countByDifficultyGrouped();

    /**
     * Count problems grouped by topic — for analytics.
     */
    @Query("""
        SELECT p.topic, COUNT(p)
        FROM Problem p WHERE p.isActive = true
        GROUP BY p.topic ORDER BY COUNT(p) DESC
        """)
    List<Object[]> countByTopicGrouped();
}
