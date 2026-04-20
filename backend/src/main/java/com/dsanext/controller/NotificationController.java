//package com.dsanext.controller;
//
//import com.dsanext.domain.entity.User;
//import com.dsanext.dto.common.ApiResponse;
//import com.dsanext.dto.common.PageResponse;
//import com.dsanext.dto.response.NotificationResponse;
//import com.dsanext.service.NotificationService;
//import com.dsanext.util.PaginationUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Notification endpoints — all require JWT.
// *
// * GET   /api/notifications             — List notifications (paginated)
// * GET   /api/notifications/unread-count — Get count of unread notifications
// * PATCH /api/notifications/{id}/read   — Mark one notification as read
// * PATCH /api/notifications/read-all   — Mark all notifications as read
// * DELETE /api/notifications/read      — Delete all read notifications
// */
//@RestController
//@RequestMapping("/notifications")
//@RequiredArgsConstructor
//public class NotificationController {
//
//    private final NotificationService notificationService;
//    private final PaginationUtils     paginationUtils;
//
//    @GetMapping
//    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
//            @AuthenticationPrincipal User user,
//            @RequestParam(required = false, defaultValue = "0") Integer page,
//            @RequestParam(required = false, defaultValue = "20") Integer size) {
//
//        Pageable pageable = paginationUtils.buildPageable(page, size, "createdAt", "desc");
//        return ResponseEntity.ok(ApiResponse.success(
//                notificationService.getUserNotifications(user.getId(), pageable)));
//    }
//
//    @GetMapping("/unread-count")
//    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
//            @AuthenticationPrincipal User user) {
//
//        long count = notificationService.getUnreadCount(user.getId());
//        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
//    }
//
//    @PatchMapping("/{id}/read")
//    public ResponseEntity<ApiResponse<Void>> markAsRead(
//            @AuthenticationPrincipal User user,
//            @PathVariable UUID id) {
//
//        notificationService.markAsRead(id, user.getId());
//        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
//    }
//
//    @PatchMapping("/read-all")
//    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
//            @AuthenticationPrincipal User user) {
//
//        int updated = notificationService.markAllAsRead(user.getId());
//        return ResponseEntity.ok(
//                ApiResponse.success("All notifications marked as read",
//                        Map.of("updatedCount", updated)));
//    }
//
//    @DeleteMapping("/read")
//    public ResponseEntity<ApiResponse<Map<String, Integer>>> clearReadNotifications(
//            @AuthenticationPrincipal User user) {
//
//        int deleted = notificationService.clearReadNotifications(user.getId());
//        return ResponseEntity.ok(
//                ApiResponse.success("Read notifications cleared",
//                        Map.of("deletedCount", deleted)));
//    }
//}



package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.response.NotificationResponse;
import com.dsanext.exception.UnauthorizedException;
import com.dsanext.service.NotificationService;
import com.dsanext.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Notification endpoints — all require a valid authenticated user.
 *
 * Every method guards against null user (token missing / expired)
 * to prevent NullPointerException when the frontend polls before
 * authentication is fully established.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PaginationUtils     paginationUtils;

    // ── Guard helper ─────────────────────────────────────────

    private void requireUser(User user) {
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }
    }

    // ── Endpoints ─────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "0")  Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        requireUser(user);
        Pageable pageable = paginationUtils.buildPageable(page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUserNotifications(user.getId(), pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal User user) {

        // Return 0 gracefully if user is not authenticated
        // (frontend polls this endpoint on every page, including during startup)
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", 0L)));
        }
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {

        requireUser(user);
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
            @AuthenticationPrincipal User user) {

        requireUser(user);
        int updated = notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "All notifications marked as read", Map.of("updatedCount", updated)));
    }

    @DeleteMapping("/read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> clearReadNotifications(
            @AuthenticationPrincipal User user) {

        requireUser(user);
        int deleted = notificationService.clearReadNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "Read notifications cleared", Map.of("deletedCount", deleted)));
    }
}
