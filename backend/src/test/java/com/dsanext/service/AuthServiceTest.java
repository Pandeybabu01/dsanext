package com.dsanext.service;

import com.dsanext.TestDataFactory;
import com.dsanext.config.JwtProperties;
import com.dsanext.domain.entity.User;
import com.dsanext.domain.entity.UserSetting;
import com.dsanext.domain.enums.Role;
import com.dsanext.dto.request.RegisterRequest;
import com.dsanext.dto.response.AuthResponse;
import com.dsanext.exception.DuplicateResourceException;
import com.dsanext.repository.UserRepository;
import com.dsanext.repository.UserSettingRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository        userRepository;
    @Mock private UserSettingRepository userSettingRepository;
    @Mock private ActivityLogService    activityLogService;

    @InjectMocks private AuthService authService;

    private JwtProperties jwtProperties;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties(
                "dsanext-test-secret-key-minimum-32-characters-long",
                3_600_000L,
                86_400_000L
        );

        passwordEncoder = new BCryptPasswordEncoder(4); // Low cost for tests

        // Manually inject since @InjectMocks doesn't handle records well
        var field = AuthService.class.getDeclaredFields();
        try {
            var jpField = AuthService.class.getDeclaredField("jwtProperties");
            jpField.setAccessible(true);
            jpField.set(authService, jwtProperties);

            var peField = AuthService.class.getDeclaredField("passwordEncoder");
            peField.setAccessible(true);
            peField.set(authService, passwordEncoder);
        } catch (Exception e) {
            // Fields injected via @InjectMocks — if not found, Mockito handles it
        }

        authService.init();
    }

    // ── Registration Tests ────────────────────────────────────

    @Test
    @DisplayName("register — success: creates user and returns JWT")
    void register_success() {
        RegisterRequest request = TestDataFactory.buildRegisterRequest();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .isActive(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userSettingRepository.save(any(UserSetting.class))).thenReturn(new UserSetting());
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getUser().getRole()).isEqualTo(Role.USER);

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals(request.getEmail()) &&
                u.getRole() == Role.USER &&
                u.isActive()
        ));
        verify(userSettingRepository).save(any(UserSetting.class));
    }

    @Test
    @DisplayName("register — fail: duplicate email throws DuplicateResourceException")
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = TestDataFactory.buildRegisterRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register — fail: duplicate username throws DuplicateResourceException")
    void register_duplicateUsername_throwsException() {
        RegisterRequest request = TestDataFactory.buildRegisterRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("username");
    }

    // ── JWT Tests ─────────────────────────────────────────────

    @Test
    @DisplayName("generateToken — returns valid JWT with correct claims")
    void generateToken_returnsValidToken() {
        User user = TestDataFactory.buildUser();

        String token = authService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature

        String email = authService.extractEmail(token);
        assertThat(email).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("validateToken — returns true for valid token")
    void validateToken_validToken_returnsTrue() {
        User user = TestDataFactory.buildUser();
        String token = authService.generateToken(user);

        assertThat(authService.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken — returns false for malformed token")
    void validateToken_malformedToken_returnsFalse() {
        assertThat(authService.validateToken("not.a.valid.jwt")).isFalse();
        assertThat(authService.validateToken("")).isFalse();
        assertThat(authService.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("validateToken — returns false for tampered token")
    void validateToken_tamperedToken_returnsFalse() {
        User user = TestDataFactory.buildUser();
        String token = authService.generateToken(user);
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";

        assertThat(authService.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername — loads user by email")
    void loadUserByUsername_success() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        var loaded = authService.loadUserByUsername(user.getEmail());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getUsername()).isEqualTo(user.getEmail());
    }
}
