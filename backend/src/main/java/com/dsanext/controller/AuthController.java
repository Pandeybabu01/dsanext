package com.dsanext.controller;

import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.request.LoginRequest;
import com.dsanext.dto.request.RegisterRequest;
import com.dsanext.dto.response.AuthResponse;
import com.dsanext.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — public, no JWT required.
 *
 * POST /api/auth/register  — Register a new user
 * POST /api/auth/login     — Login and receive JWT
 * GET  /api/auth/me        — Get current user info (requires JWT)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService          authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request, authenticationManager);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<com.dsanext.dto.response.UserResponse>> getCurrentUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            com.dsanext.domain.entity.User user) {

        return ResponseEntity.ok(
                ApiResponse.success(com.dsanext.dto.response.UserResponse.from(user)));
    }
}
