package com.dsanext.controller;

import com.dsanext.domain.entity.*;
import com.dsanext.domain.enums.*;
import com.dsanext.repository.*;
import com.dsanext.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProblemController Integration Tests")
class ProblemControllerIntegrationTest {

    @Autowired private MockMvc          mockMvc;
    @Autowired private ObjectMapper     objectMapper;
    @Autowired private UserRepository   userRepository;
    @Autowired private PlatformRepository platformRepository;
    @Autowired private ProblemRepository problemRepository;
    @Autowired private PasswordEncoder  passwordEncoder;
    @Autowired private AuthService      authService;

    private String userToken;
    private Platform platform;
    private Problem  easyProblem;
    private Problem  hardProblem;

    @BeforeEach
    void setUp() {
        // Create user and get JWT
        User user = User.builder()
                .username("testuser")
                .email("testuser@test.com")
                .passwordHash(passwordEncoder.encode("Test@1234"))
                .fullName("Test User")
                .role(Role.USER)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);
        userToken = authService.generateToken(savedUser);

        // Create platform
        platform = platformRepository.save(Platform.builder()
                .name("LeetCode")
                .baseUrl("https://leetcode.com/problems/")
                .isActive(true)
                .build());

        // Create problems
        easyProblem = problemRepository.save(Problem.builder()
                .title("Two Sum")
                .slug("two-sum")
                .topic("Array")
                .difficulty(Difficulty.EASY)
                .platform(platform)
                .isActive(true)
                .build());

        hardProblem = problemRepository.save(Problem.builder()
                .title("Trapping Rain Water")
                .slug("trapping-rain-water")
                .topic("Two Pointers")
                .difficulty(Difficulty.HARD)
                .isActive(true)
                .build());
    }

    // ── List Problems ─────────────────────────────────────────

    @Test
    @DisplayName("GET /problems — 200 returns paginated list")
    void getProblems_returns200WithList() throws Exception {
        mockMvc.perform(get("/problems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    @DisplayName("GET /problems?difficulty=EASY — filters by difficulty")
    void getProblems_filterByDifficulty_returnsOnlyEasy() throws Exception {
        mockMvc.perform(get("/problems").param("difficulty", "EASY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].difficulty",
                        everyItem(is("EASY"))));
    }

    @Test
    @DisplayName("GET /problems?difficulty=HARD — filters HARD correctly")
    void getProblems_filterByHardDifficulty() throws Exception {
        mockMvc.perform(get("/problems").param("difficulty", "HARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].difficulty",
                        everyItem(is("HARD"))));
    }

    @Test
    @DisplayName("GET /problems?search=two — full text search works")
    void getProblems_search_returnsMatchingProblems() throws Exception {
        mockMvc.perform(get("/problems").param("search", "two"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].title",
                        hasItem(containsString("Two"))));
    }

    // ── Get By Slug ───────────────────────────────────────────

    @Test
    @DisplayName("GET /problems/{slug} — 200 returns problem detail")
    void getProblemBySlug_validSlug_returns200() throws Exception {
        mockMvc.perform(get("/problems/two-sum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Two Sum"))
                .andExpect(jsonPath("$.data.difficulty").value("EASY"))
                .andExpect(jsonPath("$.data.topic").value("Array"))
                .andExpect(jsonPath("$.data.slug").value("two-sum"));
    }

    @Test
    @DisplayName("GET /problems/{slug} — 404 for unknown slug")
    void getProblemBySlug_invalidSlug_returns404() throws Exception {
        mockMvc.perform(get("/problems/nonexistent-problem"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /problems/{slug} — with JWT includes user context fields")
    void getProblemBySlug_withJwt_includesUserContext() throws Exception {
        mockMvc.perform(get("/problems/two-sum")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isBookmarked").exists())
                .andExpect(jsonPath("$.data.hasNote").exists());
    }

    // ── Difficulty Mandatory ──────────────────────────────────

    @Test
    @DisplayName("GET /problems — all problems in response have non-null difficulty")
    void getProblems_allHaveNonNullDifficulty() throws Exception {
        mockMvc.perform(get("/problems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].difficulty",
                        not(hasItem(nullValue()))));
    }

    // ── Topics ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /problems/topics — 200 returns distinct topic list")
    void getTopics_returns200WithTopics() throws Exception {
        mockMvc.perform(get("/problems/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasItem("Array")));
    }
}
