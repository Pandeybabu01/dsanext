package com.dsanext.repository;

import com.dsanext.domain.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, UUID> {

    Optional<UserSetting> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    /**
     * Update theme preference directly without loading the entity.
     */
    @Modifying
    @Query("UPDATE UserSetting s SET s.theme = :theme WHERE s.user.id = :userId")
    int updateTheme(@Param("userId") UUID userId, @Param("theme") String theme);

    /**
     * Update notification preferences.
     */
    @Modifying
    @Query("""
        UPDATE UserSetting s
        SET s.notificationsEnabled = :notifEnabled,
            s.emailNotifications   = :emailEnabled
        WHERE s.user.id = :userId
        """)
    int updateNotificationPrefs(
            @Param("userId")       UUID userId,
            @Param("notifEnabled") boolean notifEnabled,
            @Param("emailEnabled") boolean emailEnabled);
}
