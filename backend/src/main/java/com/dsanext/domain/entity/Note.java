package com.dsanext.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * User note for a specific problem.
 * One note per (user, problem) pair — enforced by unique constraint.
 */
@Entity
@Table(
    name = "notes",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_notes_user_problem",
        columnNames = {"user_id", "problem_id"}
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note extends BaseEntity {

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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
