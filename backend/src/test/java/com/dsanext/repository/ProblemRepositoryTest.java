package com.dsanext.repository;

import com.dsanext.domain.entity.*;
import com.dsanext.domain.enums.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository slice tests — uses @DataJpaTest which loads only JPA context.
 * Much faster than full SpringBootTest — no web layer, no security.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProblemRepository Slice Tests")
class ProblemRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private ProblemRepository problemRepository;
    @Autowired private PlatformRepository platformRepository;

    private Platform platform;
    private User     creator;

    @BeforeEach
    void setUp() {
        // Minimal user (creator)
        creator = em.persist(User.builder()
                .username("repouser").email("repo@test.com")
                .passwordHash("hash").fullName("Repo User")
                .role(Role.USER).isActive(true).build());

        platform = em.persist(Platform.builder()
                .name("LeetCode").baseUrl("https://leetcode.com/problems/").isActive(true).build());

        // Seed problems
        em.persist(Problem.builder().title("Two Sum")
                .slug("two-sum").topic("Array").difficulty(Difficulty.EASY)
                .platform(platform).isActive(true).createdBy(creator).build());

        em.persist(Problem.builder().title("Coin Change")
                .slug("coin-change").topic("Dynamic Programming").difficulty(Difficulty.MEDIUM)
                .platform(platform).isActive(true).createdBy(creator).build());

        em.persist(Problem.builder().title("Trapping Rain Water")
                .slug("trapping-rain-water").topic("Two Pointers").difficulty(Difficulty.HARD)
                .isActive(true).createdBy(creator).build());

        em.persist(Problem.builder().title("Valid Parentheses")
                .slug("valid-parentheses").topic("Stack").difficulty(Difficulty.EASY)
                .isActive(false).createdBy(creator).build()); // inactive

        em.flush();
    }

    // ── findBySlug ────────────────────────────────────────────

    @Test
    @DisplayName("findBySlug — finds active problem by slug")
    void findBySlug_found() {
        assertThat(problemRepository.findBySlug("two-sum")).isPresent();
        assertThat(problemRepository.findBySlug("two-sum").get().getTitle()).isEqualTo("Two Sum");
    }

    @Test
    @DisplayName("findBySlug — returns empty for unknown slug")
    void findBySlug_notFound() {
        assertThat(problemRepository.findBySlug("nonexistent")).isEmpty();
    }

    // ── findAllFiltered ───────────────────────────────────────

    @Test
    @DisplayName("findAllFiltered — returns only active problems by default")
    void findAllFiltered_returnsOnlyActive() {
        Page<Problem> page = problemRepository.findAllFiltered(
                null, null, null, null, PageRequest.of(0, 20));
        assertThat(page.getContent()).allMatch(Problem::isActive);
        assertThat(page.getTotalElements()).isEqualTo(3); // 4 seeded, 1 inactive
    }

    @Test
    @DisplayName("findAllFiltered — filters by EASY difficulty")
    void findAllFiltered_filterByEasyDifficulty() {
        Page<Problem> page = problemRepository.findAllFiltered(
                null, Difficulty.EASY, null, null, PageRequest.of(0, 20));
        assertThat(page.getContent()).allMatch(p -> p.getDifficulty() == Difficulty.EASY);
        assertThat(page.getTotalElements()).isEqualTo(1); // only "Two Sum" is active+EASY
    }

    @Test
    @DisplayName("findAllFiltered — searches by title keyword")
    void findAllFiltered_searchByTitle() {
        Page<Problem> page = problemRepository.findAllFiltered(
                "rain", null, null, null, PageRequest.of(0, 20));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).contains("Rain");
    }

    @Test
    @DisplayName("findAllFiltered — filters by topic")
    void findAllFiltered_filterByTopic() {
        Page<Problem> page = problemRepository.findAllFiltered(
                null, null, "Array", null, PageRequest.of(0, 20));
        assertThat(page.getContent()).allMatch(p -> p.getTopic().equals("Array"));
    }

    @Test
    @DisplayName("findAllFiltered — filters by platformId")
    void findAllFiltered_filterByPlatform() {
        Page<Problem> page = problemRepository.findAllFiltered(
                null, null, null, platform.getId(), PageRequest.of(0, 20));
        // Two Sum and Coin Change have platform, Trapping Rain Water does not
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).allMatch(p ->
                p.getPlatform() != null && p.getPlatform().getId().equals(platform.getId()));
    }

    // ── findDistinctTopics ────────────────────────────────────

    @Test
    @DisplayName("findDistinctTopics — returns sorted distinct topics of active problems")
    void findDistinctTopics_distinctAndSorted() {
        List<String> topics = problemRepository.findDistinctTopics();
        assertThat(topics).doesNotHaveDuplicates();
        assertThat(topics).isSorted();
        assertThat(topics).contains("Array", "Dynamic Programming", "Two Pointers");
        assertThat(topics).doesNotContain("Stack"); // inactive problem's topic excluded
    }

    // ── countByDifficulty ─────────────────────────────────────

    @Test
    @DisplayName("countByDifficulty — returns correct count per difficulty")
    void countByDifficulty_correctCounts() {
        assertThat(problemRepository.countByDifficulty(Difficulty.EASY)).isEqualTo(1);
        assertThat(problemRepository.countByDifficulty(Difficulty.MEDIUM)).isEqualTo(1);
        assertThat(problemRepository.countByDifficulty(Difficulty.HARD)).isEqualTo(1);
    }

    // ── existsBySlug ──────────────────────────────────────────

    @Test
    @DisplayName("existsBySlug — returns true for existing slug")
    void existsBySlug_exists() {
        assertThat(problemRepository.existsBySlug("two-sum")).isTrue();
    }

    @Test
    @DisplayName("existsBySlug — returns false for missing slug")
    void existsBySlug_notExists() {
        assertThat(problemRepository.existsBySlug("not-a-problem")).isFalse();
    }
}
