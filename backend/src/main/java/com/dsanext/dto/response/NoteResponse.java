package com.dsanext.dto.response;

import com.dsanext.domain.entity.Note;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NoteResponse {

    private UUID id;
    private UUID problemId;
    private String problemTitle;
    private String problemSlug;
    private String problemDifficulty;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public static NoteResponse from(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .problemId(note.getProblem().getId())
                .problemTitle(note.getProblem().getTitle())
                .problemSlug(note.getProblem().getSlug())
                .problemDifficulty(note.getProblem().getDifficulty().name())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
