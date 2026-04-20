package com.dsanext.repository;

import com.dsanext.domain.entity.Platform;
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
public interface PlatformRepository extends JpaRepository<Platform, UUID> {

    Optional<Platform> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Platform> findAllByIsActiveOrderByNameAsc(boolean isActive);

    /**
     * Search platforms by name with pagination (admin panel).
     */
    @Query("""
        SELECT p FROM Platform p
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:active IS NULL OR p.isActive = :active)
        ORDER BY p.name ASC
        """)
    Page<Platform> searchPlatforms(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable);

    /**
     * All active platforms with problem count — for admin dashboard.
     */
    @Query("""
        SELECT p.name, COUNT(pr)
        FROM Platform p
        LEFT JOIN p.problems pr ON pr.isActive = true
        WHERE p.isActive = true
        GROUP BY p.name
        ORDER BY COUNT(pr) DESC
        """)
    List<Object[]> getPlatformProblemCounts();
}
