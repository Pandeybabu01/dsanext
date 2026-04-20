package com.dsanext.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Utility to build validated {@link Pageable} objects from raw request parameters.
 * Enforces minimum page=0, minimum size=1, maximum size=100.
 */
@Component
public class PaginationUtils {

    public static final int DEFAULT_PAGE      = 0;
    public static final int DEFAULT_SIZE      = 20;
    public static final int MAX_SIZE          = 100;
    public static final String DEFAULT_SORT   = "createdAt";
    public static final String DEFAULT_DIR    = "desc";

    /**
     * Build a {@link Pageable} with validation and safe defaults.
     *
     * @param page      zero-based page index (null → 0)
     * @param size      page size (null → 20, max 100)
     * @param sortBy    field to sort by (null → createdAt)
     * @param direction "asc" or "desc" (null → desc)
     * @return validated Pageable
     */
    public Pageable buildPageable(Integer page, Integer size, String sortBy, String direction) {
        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        String safeSort = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT : sortBy;
        Sort.Direction safeDir = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize, Sort.by(safeDir, safeSort));
    }

    /**
     * Build a Pageable with defaults for sort field and direction.
     */
    public Pageable buildPageable(Integer page, Integer size) {
        return buildPageable(page, size, DEFAULT_SORT, DEFAULT_DIR);
    }
}
