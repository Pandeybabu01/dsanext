//package com.dsanext.controller;
//
//import com.dsanext.domain.entity.User;
//import com.dsanext.dto.common.ApiResponse;
//import com.dsanext.dto.response.AnalyticsResponse;
//import com.dsanext.service.AnalyticsService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
///**
// * Analytics endpoints.
// *
// * GET /api/analytics/me     — User's personal analytics dashboard (requires JWT)
// * GET /api/analytics/admin  — Platform-wide admin analytics (requires ADMIN role)
// */
//@RestController
//@RequestMapping("/analytics")
//@RequiredArgsConstructor
//public class AnalyticsController {
//
//    private final AnalyticsService analyticsService;
//
//    @GetMapping("/me")
//    public ResponseEntity<ApiResponse<AnalyticsResponse>> getUserAnalytics(
//            @AuthenticationPrincipal User user) {
//
//        return ResponseEntity.ok(ApiResponse.success(
//                analyticsService.getUserAnalytics(user.getId())));
//    }
//
//    @GetMapping("/admin")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAdminAnalytics() {
//        return ResponseEntity.ok(ApiResponse.success(
//                analyticsService.getAdminAnalytics()));
//    }
//}


package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.response.AnalyticsResponse;
import com.dsanext.repository.UserRepository;
import com.dsanext.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getUserAnalytics(
            @AuthenticationPrincipal UserDetails principal) {

        String email = principal.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                ApiResponse.success(
                        analyticsService.getUserAnalytics(user.getId())
                )
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAdminAnalytics() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        analyticsService.getAdminAnalytics()
                )
        );
    }
}