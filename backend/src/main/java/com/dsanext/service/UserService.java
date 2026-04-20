package com.dsanext.service;

import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.Role;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.UpdatePasswordRequest;
import com.dsanext.dto.request.UpdateProfileRequest;
import com.dsanext.dto.response.UserResponse;
import com.dsanext.exception.DuplicateResourceException;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.exception.ValidationException;
import com.dsanext.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final ActivityLogService activityLogService;

    // ── Profile ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return UserResponse.from(findById(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findById(userId);

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getUsername() != null && !request.getUsername().equalsIgnoreCase(user.getDisplayUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateResourceException("User", "username", request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getFullName() != null)        user.setFullName(request.getFullName());
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl());

        User saved = userRepository.save(user);
        activityLogService.log(saved, "PROFILE_UPDATED", "USER", saved.getId().toString(), null);

        log.info("Profile updated for user: {}", saved.getId());
        return UserResponse.from(saved);
    }

    @Transactional
    public void updatePassword(UUID userId, UpdatePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("New password and confirmation do not match");
        }

        User user = findById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        activityLogService.log(user, "PASSWORD_CHANGED", "USER", userId.toString(), null);
        log.info("Password changed for user: {}", userId);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = findById(userId);
        activityLogService.log(user, "ACCOUNT_DELETED", "USER", userId.toString(),
                Map.of("email", user.getEmail()));
        userRepository.delete(user);
        log.info("Account deleted: {}", userId);
    }

    // ── Admin user management ────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(String search, Role role,
            Boolean active, Pageable pageable) {
        return PageResponse.from(
                userRepository.searchUsers(search, role, active, pageable)
                        .map(UserResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return UserResponse.from(findById(userId));
    }

    @Transactional
    public UserResponse setActiveStatus(UUID targetId, boolean active, User admin) {
        User target = findById(targetId);

        if (target.getRole() == Role.ADMIN && !active) {
            throw new ValidationException("Cannot block an admin user");
        }

        userRepository.updateActiveStatus(targetId, active);
        target.setActive(active);

        String action = active ? "USER_UNBLOCKED" : "USER_BLOCKED";
        activityLogService.log(admin, action, "USER", targetId.toString(),
                Map.of("targetEmail", target.getEmail()));

        log.info("User {} {} by admin {}", targetId, action, admin.getId());
        return UserResponse.from(target);
    }

    @Transactional
    public UserResponse updateRole(UUID targetId, Role role, User admin) {
        User target = findById(targetId);

        if (target.getId().equals(admin.getId())) {
            throw new ValidationException("Admins cannot change their own role");
        }

        userRepository.updateRole(targetId, role);
        target.setRole(role);

        activityLogService.log(admin, "USER_ROLE_CHANGED", "USER", targetId.toString(),
                Map.of("targetEmail", target.getEmail(), "newRole", role.name()));

        log.info("Role of user {} changed to {} by admin {}", targetId, role, admin.getId());
        return UserResponse.from(target);
    }

    @Transactional
    public void deleteUser(UUID targetId, User admin) {
        User target = findById(targetId);

        if (target.getRole() == Role.ADMIN) {
            throw new ValidationException("Cannot delete an admin account");
        }

        activityLogService.log(admin, "USER_DELETED", "USER", targetId.toString(),
                Map.of("deletedEmail", target.getEmail()));

        userRepository.delete(target);
        log.info("User {} deleted by admin {}", targetId, admin.getId());
    }

    // ── Helpers ──────────────────────────────────────────────

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
