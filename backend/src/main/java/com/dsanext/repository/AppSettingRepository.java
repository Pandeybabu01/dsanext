package com.dsanext.repository;

import com.dsanext.domain.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, UUID> {

    Optional<AppSetting> findBySettingKey(String settingKey);

    boolean existsBySettingKey(String settingKey);

    /**
     * All settings exposed to the frontend (is_public = true).
     * Used by unauthenticated app bootstrap call.
     */
    List<AppSetting> findAllByIsPublicTrue();

    /**
     * Bulk-update a setting value by key.
     */
    @Modifying
    @Query("""
        UPDATE AppSetting s
        SET s.settingValue = :value, s.updatedAt = CURRENT_TIMESTAMP
        WHERE s.settingKey = :key
        """)
    int updateByKey(@Param("key") String key, @Param("value") String value);

    /**
     * Find all settings in a given category prefix.
     * Example: prefix = "feature." returns all feature toggles.
     */
    @Query("SELECT s FROM AppSetting s WHERE s.settingKey LIKE CONCAT(:prefix, '%') ORDER BY s.settingKey")
    List<AppSetting> findByKeyPrefix(@Param("prefix") String prefix);
}
