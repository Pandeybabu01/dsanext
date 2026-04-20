package com.dsanext;

import com.dsanext.domain.entity.*;
import com.dsanext.domain.enums.*;
import com.dsanext.dto.request.*;

import java.util.UUID;

/**
 * Shared test fixture factory for DSANext tests.
 * Provides consistent, reusable test objects across all test classes.
 */
public class TestDataFactory {

    // ── Users ────────────────────────────────────────────────

    public static User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@dsanext.com")
                .passwordHash("$2a$10$hashed_password")
                .fullName("Test User")
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    public static User buildAdmin() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("admin@dsanext.com")
                .passwordHash("$2a$10$hashed_password")
                .fullName("DSANext Admin")
                .role(Role.ADMIN)
                .isActive(true)
                .build();
    }

    public static User buildInactiveUser() {
        User user = buildUser();
        user.setActive(false);
        return user;
    }

    // ── Platforms ─────────────────────────────────────────────

    public static Platform buildPlatform() {
        return Platform.builder()
                .id(UUID.randomUUID())
                .name("LeetCode")
                .baseUrl("https://leetcode.com/problems/")
                .iconUrl("https://leetcode.com/favicon.ico")
                .isActive(true)
                .build();
    }

    // ── Problems ──────────────────────────────────────────────

    public static Problem buildEasyProblem(Platform platform, User creator) {
        return Problem.builder()
                .id(UUID.randomUUID())
                .title("Two Sum")
                .slug("two-sum")
                .description("Given an array of integers...")
                .topic("Array")
                .difficulty(Difficulty.EASY)
                .externalUrl("https://leetcode.com/problems/two-sum/")
                .platform(platform)
                .isActive(true)
                .createdBy(creator)
                .build();
    }

    public static Problem buildMediumProblem(Platform platform, User creator) {
        return Problem.builder()
                .id(UUID.randomUUID())
                .title("Add Two Numbers")
                .slug("add-two-numbers")
                .description("Add two linked lists...")
                .topic("Linked List")
                .difficulty(Difficulty.MEDIUM)
                .externalUrl("https://leetcode.com/problems/add-two-numbers/")
                .platform(platform)
                .isActive(true)
                .createdBy(creator)
                .build();
    }

    public static Problem buildHardProblem(Platform platform, User creator) {
        return Problem.builder()
                .id(UUID.randomUUID())
                .title("Trapping Rain Water")
                .slug("trapping-rain-water")
                .description("Compute trapped water...")
                .topic("Two Pointers")
                .difficulty(Difficulty.HARD)
                .isActive(true)
                .createdBy(creator)
                .build();
    }

    // ── Progress ──────────────────────────────────────────────

    public static Progress buildProgress(User user, Problem problem) {
        return Progress.builder()
                .id(UUID.randomUUID())
                .user(user)
                .problem(problem)
                .status(ProgressStatus.NOT_STARTED)
                .attemptCount(0)
                .build();
    }

    public static Progress buildSolvedProgress(User user, Problem problem) {
        Progress progress = buildProgress(user, problem);
        progress.recordAttempt();
        progress.markSolved();
        return progress;
    }

    // ── Notes ─────────────────────────────────────────────────

    public static Note buildNote(User user, Problem problem) {
        return Note.builder()
                .id(UUID.randomUUID())
                .user(user)
                .problem(problem)
                .content("Use a hash map to store complements. O(n) time, O(n) space.")
                .build();
    }

    // ── Bookmarks ─────────────────────────────────────────────

    public static Bookmark buildBookmark(User user, Problem problem) {
        return Bookmark.builder()
                .id(UUID.randomUUID())
                .user(user)
                .problem(problem)
                .build();
    }

    // ── Request DTOs ──────────────────────────────────────────

    public static RegisterRequest buildRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("newuser@dsanext.com");
        req.setPassword("NewUser@123");
        req.setFullName("New User");
        return req;
    }

    public static LoginRequest buildLoginRequest() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@dsanext.com");
        req.setPassword("Test@123");
        return req;
    }

    public static CreateProblemRequest buildCreateProblemRequest() {
        CreateProblemRequest req = new CreateProblemRequest();
        req.setTitle("New Problem");
        req.setDescription("Problem description");
        req.setTopic("Array");
        req.setDifficulty(Difficulty.EASY);
        req.setExternalUrl("https://leetcode.com/problems/new-problem/");
        return req;
    }

    public static PlatformRequest buildPlatformRequest() {
        PlatformRequest req = new PlatformRequest();
        req.setName("HackerRank");
        req.setBaseUrl("https://www.hackerrank.com/challenges/");
        req.setIconUrl("https://hrcdn.net/favicon.ico");
        req.setIsActive(true);
        return req;
    }
}
