package com.dsanext.domain.enums;

/**
 * Represents a user's progress state on a given problem.
 *
 * NOT_STARTED — default state, user has not attempted the problem
 * IN_PROGRESS — user has attempted but not solved
 * SOLVED      — user has successfully solved the problem
 * REVISIT     — user wants to revisit for review or improvement
 */
public enum ProgressStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SOLVED,
    REVISIT
}
