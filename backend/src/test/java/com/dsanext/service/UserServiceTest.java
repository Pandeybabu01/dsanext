package com.dsanext.service;

import com.dsanext.TestDataFactory;
import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.Role;
import com.dsanext.dto.request.UpdatePasswordRequest;
import com.dsanext.dto.request.UpdateProfileRequest;
import com.dsanext.dto.response.UserResponse;
import com.dsanext.exception.*;
import com.dsanext.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository     userRepository;
    @Mock private ActivityLogService activityLogService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @InjectMocks private UserService userService;

    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        user  = TestDataFactory.buildUser();
        admin = TestDataFactory.buildAdmin();

        // Inject password encoder
        try {
            var peField = UserService.class.getDeclaredField("passwordEncoder");
            peField.setAccessible(true);
            peField.set(userService, passwordEncoder);
        } catch (Exception ignored) {}
    }

    // ── Profile Update ─────────────────────────────────────────

    @Test
    @DisplayName("updateProfile — success: updates provided fields")
    void updateProfile_success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        UserResponse response = userService.updateProfile(user.getId(), request);

        assertThat(response.getFullName()).isEqualTo("Updated Name");
        assertThat(response.getEmail()).isEqualTo(user.getEmail()); // unchanged
    }

    @Test
    @DisplayName("updateProfile — fail: new email already taken")
    void updateProfile_emailTaken_throwsException() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("taken@example.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(user.getId(), request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");
    }

    // ── Password Change ────────────────────────────────────────

    @Test
    @DisplayName("updatePassword — success: hashes and saves new password")
    void updatePassword_success() {
        String rawCurrentPassword = "Current@123";
        user.setPasswordHash(passwordEncoder.encode(rawCurrentPassword));

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword(rawCurrentPassword);
        request.setNewPassword("NewSecure@456");
        request.setConfirmPassword("NewSecure@456");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        userService.updatePassword(user.getId(), request);

        verify(userRepository).save(argThat(u ->
                passwordEncoder.matches("NewSecure@456", u.getPasswordHash())
        ));
    }

    @Test
    @DisplayName("updatePassword — fail: wrong current password")
    void updatePassword_wrongCurrentPassword_throwsException() {
        user.setPasswordHash(passwordEncoder.encode("Correct@123"));

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("Wrong@123");
        request.setNewPassword("NewSecure@456");
        request.setConfirmPassword("NewSecure@456");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updatePassword(user.getId(), request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Current password");
    }

    @Test
    @DisplayName("updatePassword — fail: passwords do not match")
    void updatePassword_mismatch_throwsException() {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("Current@123");
        request.setNewPassword("NewSecure@456");
        request.setConfirmPassword("Different@789");

        assertThatThrownBy(() -> userService.updatePassword(user.getId(), request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("match");
    }

    // ── Admin User Management ──────────────────────────────────

    @Test
    @DisplayName("setActiveStatus — success: blocks regular user")
    void setActiveStatus_blockUser_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.updateActiveStatus(user.getId(), false)).thenReturn(1);
        doNothing().when(activityLogService).log(any(), anyString(), anyString(), anyString(), any());

        userService.setActiveStatus(user.getId(), false, admin);

        verify(userRepository).updateActiveStatus(user.getId(), false);
        verify(activityLogService).log(eq(admin), eq("USER_BLOCKED"), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("setActiveStatus — fail: cannot block admin user")
    void setActiveStatus_blockAdmin_throwsException() {
        User anotherAdmin = TestDataFactory.buildAdmin();
        when(userRepository.findById(anotherAdmin.getId())).thenReturn(Optional.of(anotherAdmin));

        assertThatThrownBy(() -> userService.setActiveStatus(anotherAdmin.getId(), false, admin))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Cannot block an admin");
    }

    @Test
    @DisplayName("updateRole — fail: admin cannot change own role")
    void updateRole_selfRoleChange_throwsException() {
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.updateRole(admin.getId(), Role.USER, admin))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("own role");
    }

    @Test
    @DisplayName("deleteUser — fail: cannot delete admin account")
    void deleteUser_deleteAdmin_throwsException() {
        User anotherAdmin = TestDataFactory.buildAdmin();
        when(userRepository.findById(anotherAdmin.getId())).thenReturn(Optional.of(anotherAdmin));

        assertThatThrownBy(() -> userService.deleteUser(anotherAdmin.getId(), admin))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("admin");
    }
}
