package com.dsanext.repository;

import com.dsanext.domain.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    Optional<Bookmark> findByUserIdAndProblemId(UUID userId, UUID problemId);

    boolean existsByUserIdAndProblemId(UUID userId, UUID problemId);

    long countByUserId(UUID userId);

    void deleteByUserIdAndProblemId(UUID userId, UUID problemId);

    /**
     * All bookmarks for a user with problem + platform eagerly loaded.
     * Supports filtering by difficulty and topic.
     */
    @Query("""
        SELECT b FROM Bookmark b
        JOIN FETCH b.problem p
        LEFT JOIN FETCH p.platform pl
        WHERE b.user.id = :userId
          AND (:search IS NULL OR :search = ''
               OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:topic IS NULL OR :topic = '' OR LOWER(p.topic) = LOWER(:topic))
          AND (:difficulty IS NULL
               OR CAST(p.difficulty AS string) = :difficulty)
        ORDER BY b.createdAt DESC
        """)
    Page<Bookmark> findByUserIdFiltered(
            @Param("userId")     UUID userId,
            @Param("search")     String search,
            @Param("topic")      String topic,
            @Param("difficulty") String difficulty,
            Pageable pageable);
}
