package com.dsanext.repository;

import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByRole(Role role);

    long countByIsActive(boolean isActive);

    /**
     * Search users by name, email, or username (case-insensitive).
     * Used by admin user management panel with pagination.
     */
    @Query("""
        SELECT u FROM User u
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(u.fullName)  LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.username)  LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:role IS NULL OR u.role = :role)
          AND (:active IS NULL OR u.isActive = :active)
        ORDER BY u.createdAt DESC
        """)
    Page<User> searchUsers(
            @Param("search") String search,
            @Param("role") Role role,
            @Param("active") Boolean active,
            Pageable pageable);

    /**
     * Toggle user active status (block/unblock).
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = :status WHERE u.id = :userId")
    int updateActiveStatus(@Param("userId") UUID userId, @Param("status") boolean status);

    /**
     * Update user role.
     */
    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    int updateRole(@Param("userId") UUID userId, @Param("role") Role role);
}
