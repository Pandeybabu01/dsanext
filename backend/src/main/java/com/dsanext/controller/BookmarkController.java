package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.response.BookmarkResponse;
import com.dsanext.service.BookmarkService;
import com.dsanext.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Bookmark management endpoints — all require JWT.
 *
 * GET    /api/bookmarks                    — List all bookmarks (paginated, filterable)
 * POST   /api/bookmarks/{problemId}        — Add bookmark
 * DELETE /api/bookmarks/{problemId}        — Remove bookmark
 * POST   /api/bookmarks/{problemId}/toggle — Toggle bookmark (add/remove)
 * GET    /api/bookmarks/{problemId}/status — Check if a problem is bookmarked
 */
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final PaginationUtils paginationUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookmarkResponse>>> getMyBookmarks(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        Pageable pageable = paginationUtils.buildPageable(page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(
                bookmarkService.getUserBookmarks(
                        user.getId(), search, topic, difficulty, pageable)));
    }

    @PostMapping("/{problemId}")
    public ResponseEntity<ApiResponse<BookmarkResponse>> addBookmark(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId) {

        BookmarkResponse response = bookmarkService.addBookmark(user.getId(), problemId, user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Problem bookmarked", response));
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId) {

        bookmarkService.removeBookmark(user.getId(), problemId, user);
        return ResponseEntity.ok(ApiResponse.success("Bookmark removed"));
    }

    @PostMapping("/{problemId}/toggle")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleBookmark(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId) {

        boolean isNowBookmarked = bookmarkService.toggleBookmark(user.getId(), problemId, user);
        String message = isNowBookmarked ? "Problem bookmarked" : "Bookmark removed";
        return ResponseEntity.ok(
                ApiResponse.success(message, Map.of("bookmarked", isNowBookmarked)));
    }

    @GetMapping("/{problemId}/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getBookmarkStatus(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId) {

        boolean isBookmarked = bookmarkService.isBookmarked(user.getId(), problemId);
        return ResponseEntity.ok(
                ApiResponse.success(Map.of("bookmarked", isBookmarked)));
    }
}
