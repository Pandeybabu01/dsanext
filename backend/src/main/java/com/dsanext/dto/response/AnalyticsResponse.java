package com.dsanext.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {

    // ── User analytics ───────────────────────────────────────
    private Long totalSolved;
    private Long totalInProgress;
    private Long totalRevisit;
    private Long totalBookmarks;
    private Long totalNotes;

    // Difficulty breakdown: { "EASY": 10, "MEDIUM": 5, "HARD": 2 }
    private Map<String, Long> solvedByDifficulty;

    // Topic breakdown: { "Array": 8, "DP": 4, ... }
    private Map<String, Long> solvedByTopic;

    // Platform breakdown: { "LeetCode": 12, "Codeforces": 3, ... }
    private Map<String, Long> solvedByPlatform;

    // Recent solved problems (last 5)
    private List<ProgressResponse> recentlySolved;

    // Daily solved count for the last 30 days
    private List<DailyStat> dailyStats;

    // ── Admin analytics ──────────────────────────────────────
    private Long totalUsers;
    private Long totalActiveUsers;
    private Long totalProblems;
    private Long totalActiveProblems;
    private Long totalProgressEntries;

    private Map<String, Long> problemsByDifficulty;
    private Map<String, Long> problemsByPlatform;
    private Map<String, Long> usersByRole;

    // ── Nested types ─────────────────────────────────────────

    @Data
    @Builder
    public static class DailyStat {
        private String date;   // yyyy-MM-dd
        private long count;
    }
}
