package com.dsanext.domain.entity;

import com.dsanext.domain.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a specific user's progress on a specific problem.
 * The combination (user_id, problem_id) is unique — enforced by DB and JPA.
 */
@Entity
@Table(
    name = "progress",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_progress_user_problem",
        columnNames = {"user_id", "problem_id"}
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    @Column(name = "first_attempted_at")
    private Instant firstAttemptedAt;

    @Column(name = "solved_at")
    private Instant solvedAt;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    // ── Domain Logic ────────────────────────────────────────

    /**
     * Increment attempt count and set firstAttemptedAt on the first attempt.
     */
    public void recordAttempt() {
        if (this.firstAttemptedAt == null) {
            this.firstAttemptedAt = Instant.now();
        }
        this.attemptCount++;
        this.updatedAt = Instant.now();
    }

    /**
     * Mark the problem as solved, recording the solved timestamp.
     */
    public void markSolved() {
        if (this.status != ProgressStatus.SOLVED) {
            this.status = ProgressStatus.SOLVED;
            this.solvedAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }
}
