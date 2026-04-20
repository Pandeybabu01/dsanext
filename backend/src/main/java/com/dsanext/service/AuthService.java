package com.dsanext.service;

import com.dsanext.config.JwtProperties;
import com.dsanext.domain.entity.User;
import com.dsanext.domain.entity.UserSetting;
import com.dsanext.domain.enums.Role;
import com.dsanext.dto.request.LoginRequest;
import com.dsanext.dto.request.RegisterRequest;
import com.dsanext.dto.response.AuthResponse;
import com.dsanext.dto.response.UserResponse;
import com.dsanext.exception.DuplicateResourceException;
import com.dsanext.exception.UnauthorizedException;
import com.dsanext.repository.UserRepository;
import com.dsanext.repository.UserSettingRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Handles authentication, registration, and JWT token lifecycle.
 * Also implements {@link UserDetailsService} for Spring Security.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository        userRepository;
    private final UserSettingRepository userSettingRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtProperties         jwtProperties;
    private final ActivityLogService    activityLogService;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    // ── Registration ─────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);

        // Auto-create default user settings
        UserSetting settings = UserSetting.builder()
                .user(saved)
                .theme("light")
                .notificationsEnabled(true)
                .emailNotifications(true)
                .build();
        userSettingRepository.save(settings);

        log.info("New user registered: {} ({})", saved.getEmail(), saved.getId());
        activityLogService.log(saved, "USER_REGISTERED", "USER", saved.getId().toString(), null);

        String token = generateToken(saved);
        return AuthResponse.of(token, jwtProperties.expirationMs(), UserResponse.from(saved));
    }

    // ── Login ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, AuthenticationManager authManager) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been disabled. Please contact support.");
        }

        log.info("User logged in: {}", user.getEmail());
        activityLogService.log(user, "USER_LOGIN", "USER", user.getId().toString(), null);

        String token = generateToken(user);
        return AuthResponse.of(token, jwtProperties.expirationMs(), UserResponse.from(user));
    }

    // ── JWT Generation ───────────────────────────────────────

    public String generateToken(User user) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.expirationMs());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("username", user.getDisplayUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // ── JWT Validation ───────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired");
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT token unsupported");
        } catch (MalformedJwtException ex) {
            log.warn("JWT token malformed");
        } catch (SecurityException ex) {
            log.warn("JWT signature validation failed");
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty");
        }
        return false;
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ── UserDetailsService ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }

    /**
     * Load the actual User entity by email.
     * Used by JwtAuthFilter to set the correct principal type so that
     * @AuthenticationPrincipal resolves to com.dsanext.domain.entity.User
     * in controllers instead of Spring's UserDetails wrapper.
     */
    @Transactional(readOnly = true)
    public com.dsanext.domain.entity.User loadUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}