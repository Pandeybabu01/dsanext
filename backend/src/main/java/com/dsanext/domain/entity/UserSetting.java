package com.dsanext.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Per-user preferences and connected platform usernames.
 * One-to-one with User; created automatically on registration.
 */
@Entity
@Table(name = "user_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String theme = "light";

    @Column(name = "notifications_enabled", nullable = false)
    @Builder.Default
    private boolean notificationsEnabled = true;

    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private boolean emailNotifications = true;

    /** LeetCode username */
    @Column(name = "lc_username", length = 100)
    private String lcUsername;

    /** Codeforces username */
    @Column(name = "cf_username", length = 100)
    private String cfUsername;

    /** HackerRank username */
    @Column(name = "hr_username", length = 100)
    private String hrUsername;

    /** InterviewBit username */
    @Column(name = "ib_username", length = 100)
    private String ibUsername;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
