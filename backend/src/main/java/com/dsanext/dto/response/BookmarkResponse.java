package com.dsanext.dto.response;

import com.dsanext.domain.entity.Bookmark;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BookmarkResponse {

    private UUID id;
    private ProblemResponse problem;
    private Instant createdAt;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .problem(ProblemResponse.from(bookmark.getProblem()))
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
