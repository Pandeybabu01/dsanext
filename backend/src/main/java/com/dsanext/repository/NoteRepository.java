package com.dsanext.repository;

import com.dsanext.domain.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    Optional<Note> findByUserIdAndProblemId(UUID userId, UUID problemId);

    boolean existsByUserIdAndProblemId(UUID userId, UUID problemId);

    long countByUserId(UUID userId);

    /**
     * All notes for a user with problem details, supports keyword search.
     */
    @Query("""
        SELECT n FROM Note n
        JOIN FETCH n.problem p
        LEFT JOIN FETCH p.platform pl
        WHERE n.user.id = :userId
          AND (:search IS NULL OR :search = ''
               OR LOWER(p.title)   LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY n.updatedAt DESC
        """)
    Page<Note> findByUserIdWithSearch(
            @Param("userId") UUID userId,
            @Param("search") String search,
            Pageable pageable);
}
