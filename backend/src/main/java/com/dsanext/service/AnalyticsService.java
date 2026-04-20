//package com.dsanext.service;
//
//import com.dsanext.domain.enums.Difficulty;
//import com.dsanext.domain.enums.ProgressStatus;
//import com.dsanext.domain.enums.Role;
//import com.dsanext.dto.response.AnalyticsResponse;
//import com.dsanext.dto.response.ProgressResponse;
//import com.dsanext.repository.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AnalyticsService {
//
//    private final ProgressRepository progressRepository;
//    private final ProblemRepository  problemRepository;
//    private final UserRepository     userRepository;
//    private final BookmarkRepository bookmarkRepository;
//    private final NoteRepository     noteRepository;
//
//    // ── User analytics ───────────────────────────────────────
//
//    @Transactional(readOnly = true)
//    public AnalyticsResponse getUserAnalytics(UUID userId) {
//
//        // Counts
//        long totalSolved     = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.SOLVED);
//        long totalInProgress = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.IN_PROGRESS);
//        long totalRevisit    = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.REVISIT);
//        long totalBookmarks  = bookmarkRepository.countByUserId(userId);
//        long totalNotes      = noteRepository.countByUserId(userId);
//
//        // Difficulty breakdown
//        Map<String, Long> solvedByDifficulty = buildDifficultyMap(userId);
//
//        // Topic breakdown
//        Map<String, Long> solvedByTopic = buildTopicMap(userId);
//
//        // Recently solved
//        List<ProgressResponse> recentlySolved = progressRepository
//                .findRecentSolved(userId, PageRequest.of(0, 5))
//                .stream()
//                .map(ProgressResponse::from)
//                .toList();
//
//        // Daily stats (last 30 days)
//        List<AnalyticsResponse.DailyStat> dailyStats = buildDailyStats();
//
//        return AnalyticsResponse.builder()
//                .totalSolved(totalSolved)
//                .totalInProgress(totalInProgress)
//                .totalRevisit(totalRevisit)
//                .totalBookmarks(totalBookmarks)
//                .totalNotes(totalNotes)
//                .solvedByDifficulty(solvedByDifficulty)
//                .solvedByTopic(solvedByTopic)
//                .recentlySolved(recentlySolved)
//                .dailyStats(dailyStats)
//                .build();
//    }
//
//    // ── Admin analytics ──────────────────────────────────────
//
//    @Transactional(readOnly = true)
//    public AnalyticsResponse getAdminAnalytics() {
//
//        long totalUsers        = userRepository.count();
//        long totalActiveUsers  = userRepository.countByIsActive(true);
//        long totalProblems     = problemRepository.count();
//        long totalActiveProblems = problemRepository.countByIsActive(true);
//        long totalProgress     = progressRepository.count();
//
//        // Problems by difficulty
//        Map<String, Long> problemsByDifficulty = new LinkedHashMap<>();
//        for (Difficulty d : Difficulty.values()) {
//            problemsByDifficulty.put(d.name(), problemRepository.countByDifficulty(d));
//        }
//
//        // Problems by platform
//        Map<String, Long> problemsByPlatform = new LinkedHashMap<>();
//        problemRepository.countByDifficultyGrouped(); // reuse query context
//
//        // Users by role
//        Map<String, Long> usersByRole = new LinkedHashMap<>();
//        usersByRole.put("USER",  userRepository.countByRole(Role.USER));
//        usersByRole.put("ADMIN", userRepository.countByRole(Role.ADMIN));
//
//        return AnalyticsResponse.builder()
//                .totalUsers(totalUsers)
//                .totalActiveUsers(totalActiveUsers)
//                .totalProblems(totalProblems)
//                .totalActiveProblems(totalActiveProblems)
//                .totalProgressEntries(totalProgress)
//                .problemsByDifficulty(problemsByDifficulty)
//                .usersByRole(usersByRole)
//                .dailyStats(buildDailyStats())
//                .build();
//    }
//
//    // ── Private helpers ──────────────────────────────────────
//
//    private Map<String, Long> buildDifficultyMap(UUID userId) {
//        Map<String, Long> map = new LinkedHashMap<>();
//        map.put("EASY", 0L); map.put("MEDIUM", 0L); map.put("HARD", 0L);
//
//        progressRepository.getProgressByDifficulty(userId).forEach(row -> {
//            String difficulty = row[0].toString();
//            String status     = row[1].toString();
//            long   count      = ((Number) row[2]).longValue();
//            if ("SOLVED".equals(status)) {
//                map.merge(difficulty, count, Long::sum);
//            }
//        });
//
//        return map;
//    }
//
//    private Map<String, Long> buildTopicMap(UUID userId) {
//        Map<String, Long> map = new LinkedHashMap<>();
//        progressRepository.getProgressByTopic(userId).forEach(row -> {
//            String topic      = row[0].toString();
//            long   solvedCount = ((Number) row[1]).longValue();
//            map.put(topic, solvedCount);
//        });
//        return map;
//    }
//
//    private List<AnalyticsResponse.DailyStat> buildDailyStats() {
//        return progressRepository.getDailySolvedStats().stream()
//                .map(row -> AnalyticsResponse.DailyStat.builder()
//                        .date(row[0].toString())
//                        .count(((Number) row[1]).longValue())
//                        .build())
//                .collect(Collectors.toList());
//    }
//}


package com.dsanext.service;

import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.Difficulty;
import com.dsanext.domain.enums.ProgressStatus;
import com.dsanext.domain.enums.Role;
import com.dsanext.dto.response.AnalyticsResponse;
import com.dsanext.dto.response.ProgressResponse;
import com.dsanext.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ProgressRepository progressRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NoteRepository noteRepository;

    // ───────────────── USER ANALYTICS ─────────────────

    @Transactional(readOnly = true)
    public AnalyticsResponse getUserAnalytics(UUID userId) {

        long totalSolved = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.SOLVED);
        long totalInProgress = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.IN_PROGRESS);
        long totalRevisit = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.REVISIT);
        long totalBookmarks = bookmarkRepository.countByUserId(userId);
        long totalNotes = noteRepository.countByUserId(userId);

        Map<String, Long> solvedByDifficulty = buildDifficultyMap(userId);
        Map<String, Long> solvedByTopic = buildTopicMap(userId);

        List<ProgressResponse> recentlySolved =
                progressRepository.findRecentSolved(userId, PageRequest.of(0, 5))
                        .stream()
                        .map(ProgressResponse::from)
                        .toList();

        return AnalyticsResponse.builder()
                .totalSolved(totalSolved)
                .totalInProgress(totalInProgress)
                .totalRevisit(totalRevisit)
                .totalBookmarks(totalBookmarks)
                .totalNotes(totalNotes)
                .solvedByDifficulty(solvedByDifficulty)
                .solvedByTopic(solvedByTopic)
                .recentlySolved(recentlySolved)
                .dailyStats(buildDailyStats())
                .build();
    }

    // ───────────────── NEW FIX (IMPORTANT) ─────────────────

    @Transactional(readOnly = true)
    public AnalyticsResponse getUserAnalyticsByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return getUserAnalytics(user.getId());
    }

    // ───────────────── ADMIN ANALYTICS ─────────────────

    @Transactional(readOnly = true)
    public AnalyticsResponse getAdminAnalytics() {

        long totalUsers = userRepository.count();
        long totalActiveUsers = userRepository.countByIsActive(true);
        long totalProblems = problemRepository.count();
        long totalActiveProblems = problemRepository.countByIsActive(true);
        long totalProgress = progressRepository.count();

        Map<String, Long> problemsByDifficulty = new LinkedHashMap<>();
        for (Difficulty d : Difficulty.values()) {
            problemsByDifficulty.put(d.name(), problemRepository.countByDifficulty(d));
        }

        Map<String, Long> usersByRole = new LinkedHashMap<>();
        usersByRole.put("USER", userRepository.countByRole(Role.USER));
        usersByRole.put("ADMIN", userRepository.countByRole(Role.ADMIN));

        return AnalyticsResponse.builder()
                .totalUsers(totalUsers)
                .totalActiveUsers(totalActiveUsers)
                .totalProblems(totalProblems)
                .totalActiveProblems(totalActiveProblems)
                .totalProgressEntries(totalProgress)
                .problemsByDifficulty(problemsByDifficulty)
                .usersByRole(usersByRole)
                .dailyStats(buildDailyStats())
                .build();
    }

    // ───────────────── HELPERS ─────────────────

    private Map<String, Long> buildDifficultyMap(UUID userId) {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("EASY", 0L);
        map.put("MEDIUM", 0L);
        map.put("HARD", 0L);

        progressRepository.getProgressByDifficulty(userId).forEach(row -> {
            String difficulty = String.valueOf(row[0]);
            String status = String.valueOf(row[1]);
            long count = ((Number) row[2]).longValue();

            if (ProgressStatus.SOLVED.name().equals(status)) {
                map.merge(difficulty, count, Long::sum);
            }
        });

        return map;
    }

    private Map<String, Long> buildTopicMap(UUID userId) {
        Map<String, Long> map = new LinkedHashMap<>();

        progressRepository.getProgressByTopic(userId).forEach(row -> {
            String topic = String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();
            map.put(topic, count);
        });

        return map;
    }

    private List<AnalyticsResponse.DailyStat> buildDailyStats() {
        return progressRepository.getDailySolvedStats().stream()
                .map(row -> AnalyticsResponse.DailyStat.builder()
                        .date(String.valueOf(row[0]))
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
}