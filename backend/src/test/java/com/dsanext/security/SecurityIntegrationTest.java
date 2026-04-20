package com.dsanext.security;

import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.Role;
import com.dsanext.repository.UserRepository;
import com.dsanext.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests — verifies the entire security filter chain.
 * Tests public access, JWT protection, and role enforcement.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Security Filter Chain Integration Tests")
class SecurityIntegrationTest {

    @Autowired private MockMvc        mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthService    authService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .username("secuser").email("secuser@test.com")
                .passwordHash(passwordEncoder.encode("Test@1234"))
                .fullName("Security User").role(Role.USER).isActive(true).build());
        userToken = authService.generateToken(user);

        User admin = userRepository.save(User.builder()
                .username("secadmin").email("secadmin@test.com")
                .passwordHash(passwordEncoder.encode("Admin@1234"))
                .fullName("Security Admin").role(Role.ADMIN).isActive(true).build());
        adminToken = authService.generateToken(admin);
    }

    // ── Public Endpoints ──────────────────────────────────────

    @Test
    @DisplayName("Public: GET /problems — accessible without token")
    void publicEndpoint_problems_noAuthRequired() throws Exception {
        mockMvc.perform(get("/problems"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public: GET /platforms — accessible without token")
    void publicEndpoint_platforms_noAuthRequired() throws Exception {
        mockMvc.perform(get("/platforms"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public: GET /settings/public — accessible without token")
    void publicEndpoint_publicSettings_noAuthRequired() throws Exception {
        mockMvc.perform(get("/settings/public"))
                .andExpect(status().isOk());
    }

    // ── Protected Endpoints — No Token ─────────────────────────

    @Test
    @DisplayName("Protected: GET /progress — 401 without token")
    void protectedEndpoint_progress_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/progress"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Protected: GET /notes — 401 without token")
    void protectedEndpoint_notes_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected: GET /bookmarks — 401 without token")
    void protectedEndpoint_bookmarks_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/bookmarks"))
                .andExpect(status().isUnauthorized());
    }

    // ── Protected Endpoints — With Valid Token ─────────────────

    @Test
    @DisplayName("Protected: GET /progress — 200 with valid USER token")
    void protectedEndpoint_progress_returns200WithUserToken() throws Exception {
        mockMvc.perform(get("/progress")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected: GET /analytics/me — 200 with valid USER token")
    void protectedEndpoint_analytics_returns200WithUserToken() throws Exception {
        mockMvc.perform(get("/analytics/me")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ── Admin Endpoints — Role Enforcement ─────────────────────

    @Test
    @DisplayName("Admin: GET /admin/users — 403 with USER token")
    void adminEndpoint_users_returns403WithUserToken() throws Exception {
        mockMvc.perform(get("/admin/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Admin: GET /admin/users — 200 with ADMIN token")
    void adminEndpoint_users_returns200WithAdminToken() throws Exception {
        mockMvc.perform(get("/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin: GET /analytics/admin — 403 with USER token")
    void adminEndpoint_analytics_returns403WithUserToken() throws Exception {
        mockMvc.perform(get("/analytics/admin")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin: GET /analytics/admin — 200 with ADMIN token")
    void adminEndpoint_analytics_returns200WithAdminToken() throws Exception {
        mockMvc.perform(get("/analytics/admin")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ── Token Validation ──────────────────────────────────────

    @Test
    @DisplayName("Security: tampered token returns 401")
    void tamperedToken_returns401() throws Exception {
        String tampered = userToken.substring(0, userToken.lastIndexOf('.') + 1) + "invalidsig";

        mockMvc.perform(get("/progress")
                .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: malformed Authorization header returns 401")
    void malformedAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/progress")
                .header("Authorization", "NotBearer token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: no 'Bearer' prefix returns 401")
    void noBearerPrefix_returns401() throws Exception {
        mockMvc.perform(get("/progress")
                .header("Authorization", userToken))
                .andExpect(status().isUnauthorized());
    }

    // ── Inactive User ─────────────────────────────────────────

    @Test
    @DisplayName("Security: blocked user token returns 401")
    void blockedUser_returns401() throws Exception {
        User blocked = userRepository.save(User.builder()
                .username("blockeduser").email("blocked@test.com")
                .passwordHash(passwordEncoder.encode("Test@1234"))
                .fullName("Blocked User").role(Role.USER).isActive(false).build());
        String blockedToken = authService.generateToken(blocked);

        mockMvc.perform(get("/progress")
                .header("Authorization", "Bearer " + blockedToken))
                .andExpect(status().isUnauthorized());
    }
}
