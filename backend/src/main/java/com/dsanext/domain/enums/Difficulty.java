package com.dsanext.domain.enums;

/**
 * Problem difficulty levels.
 * EASY   🟢 — beginner-friendly problems
 * MEDIUM 🟡 — intermediate problems
 * HARD   🔴 — advanced problems
 *
 * Stored in PostgreSQL as the difficulty_level enum type.
 */
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD
}
