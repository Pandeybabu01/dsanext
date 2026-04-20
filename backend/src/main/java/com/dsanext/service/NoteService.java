package com.dsanext.service;

import com.dsanext.domain.entity.Note;
import com.dsanext.domain.entity.Problem;
import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.request.NoteRequest;
import com.dsanext.dto.response.NoteResponse;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.exception.UnauthorizedException;
import com.dsanext.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository     noteRepository;
    private final ProblemService     problemService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> getUserNotes(UUID userId, String search, Pageable pageable) {
        return PageResponse.from(
                noteRepository.findByUserIdWithSearch(userId, search, pageable)
                        .map(NoteResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteForProblem(UUID userId, UUID problemId) {
        Note note = noteRepository.findByUserIdAndProblemId(userId, problemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Note", "userId+problemId", userId + "+" + problemId));
        return NoteResponse.from(note);
    }

    /**
     * Create or update a note for a problem.
     * One note per user per problem — upsert semantics.
     */
    @Transactional
    public NoteResponse upsertNote(UUID userId, UUID problemId, NoteRequest request, User user) {
        Problem problem = problemService.findById(problemId);

        Note note = noteRepository.findByUserIdAndProblemId(userId, problemId)
                .orElseGet(() -> Note.builder()
                        .user(user)
                        .problem(problem)
                        .build());

        note.setContent(request.getContent());
        Note saved = noteRepository.save(note);

        activityLogService.log(user, "NOTE_SAVED", "PROBLEM", problemId.toString(),
                Map.of("problemTitle", problem.getTitle()));

        return NoteResponse.from(saved);
    }

    @Transactional
    public void deleteNote(UUID userId, UUID problemId, User user) {
        Note note = noteRepository.findByUserIdAndProblemId(userId, problemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Note", "userId+problemId", userId + "+" + problemId));

        if (!note.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not own this note");
        }

        noteRepository.delete(note);
        activityLogService.log(user, "NOTE_DELETED", "PROBLEM", problemId.toString(), null);
    }
}
