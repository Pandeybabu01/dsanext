package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.NoteRequest;
import com.dsanext.dto.response.NoteResponse;
import com.dsanext.service.NoteService;
import com.dsanext.util.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Note management endpoints — all require JWT.
 *
 * GET    /api/notes                    — List all user notes (paginated, searchable)
 * GET    /api/notes/problem/{problemId} — Get note for specific problem
 * PUT    /api/notes/problem/{problemId} — Create or update note for a problem
 * DELETE /api/notes/problem/{problemId} — Delete note for a problem
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService     noteService;
    private final PaginationUtils paginationUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NoteResponse>>> getMyNotes(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        Pageable pageable = paginationUtils.buildPageable(page, size, "updatedAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(
                noteService.getUserNotes(user.getId(), search, pageable)));
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<ApiResponse<NoteResponse>> getNoteForProblem(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId) {

        return ResponseEntity.ok(ApiResponse.success(
                noteService.getNoteForProblem(user.getId(), problemId)));
    }

    @PutMapping("/problem/{problemId}")
    public ResponseEntity<ApiResponse<NoteResponse>> upsertNote(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId,
            @Valid @RequestBody NoteRequest request) {

        NoteResponse response = noteService.upsertNote(user.getId(), problemId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Note saved", response));
    }

    @DeleteMapping("/problem/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @AuthenticationPrincipal User user,
            @PathVariable UUID problemId) {

        noteService.deleteNote(user.getId(), problemId, user);
        return ResponseEntity.ok(ApiResponse.success("Note deleted"));
    }
}
