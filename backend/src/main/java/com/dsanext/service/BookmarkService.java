package com.dsanext.service;

import com.dsanext.domain.entity.Bookmark;
import com.dsanext.domain.entity.Problem;
import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.response.BookmarkResponse;
import com.dsanext.exception.DuplicateResourceException;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.repository.BookmarkRepository;
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
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ProblemService     problemService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public PageResponse<BookmarkResponse> getUserBookmarks(UUID userId, String search,
            String topic, String difficulty, Pageable pageable) {
        return PageResponse.from(
                bookmarkRepository.findByUserIdFiltered(userId, search, topic, difficulty, pageable)
                        .map(BookmarkResponse::from)
        );
    }

    @Transactional
    public BookmarkResponse addBookmark(UUID userId, UUID problemId, User user) {
        if (bookmarkRepository.existsByUserIdAndProblemId(userId, problemId)) {
            throw new DuplicateResourceException("Bookmark already exists for this problem");
        }

        Problem problem = problemService.findById(problemId);

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .problem(problem)
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);

        activityLogService.log(user, "BOOKMARK_ADDED", "PROBLEM", problemId.toString(),
                Map.of("problemTitle", problem.getTitle()));

        return BookmarkResponse.from(saved);
    }

    @Transactional
    public void removeBookmark(UUID userId, UUID problemId, User user) {
        if (!bookmarkRepository.existsByUserIdAndProblemId(userId, problemId)) {
            throw new ResourceNotFoundException("Bookmark", "userId+problemId",
                    userId + "+" + problemId);
        }

        bookmarkRepository.deleteByUserIdAndProblemId(userId, problemId);
        activityLogService.log(user, "BOOKMARK_REMOVED", "PROBLEM", problemId.toString(), null);
    }

    /**
     * Toggle — add if absent, remove if present.
     * Returns true if bookmarked after the operation, false if removed.
     */
    @Transactional
    public boolean toggleBookmark(UUID userId, UUID problemId, User user) {
        if (bookmarkRepository.existsByUserIdAndProblemId(userId, problemId)) {
            bookmarkRepository.deleteByUserIdAndProblemId(userId, problemId);
            activityLogService.log(user, "BOOKMARK_REMOVED", "PROBLEM", problemId.toString(), null);
            return false;
        } else {
            Problem problem = problemService.findById(problemId);
            Bookmark bookmark = Bookmark.builder().user(user).problem(problem).build();
            bookmarkRepository.save(bookmark);
            activityLogService.log(user, "BOOKMARK_ADDED", "PROBLEM", problemId.toString(),
                    Map.of("problemTitle", problem.getTitle()));
            return true;
        }
    }

    public boolean isBookmarked(UUID userId, UUID problemId) {
        return bookmarkRepository.existsByUserIdAndProblemId(userId, problemId);
    }
}
