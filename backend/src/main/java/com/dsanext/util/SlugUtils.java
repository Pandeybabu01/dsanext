package com.dsanext.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility for generating URL-safe slugs from strings.
 * Example: "Two Sum" → "two-sum"
 */
@Component
public class SlugUtils {

    private static final Pattern NON_LATIN     = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE    = Pattern.compile("[\\s]+");
    private static final Pattern MULTI_DASH    = Pattern.compile("-{2,}");
    private static final Pattern LEADING_DASH  = Pattern.compile("^-|-$");

    /**
     * Convert a title to a URL-safe slug.
     *
     * @param input raw title string
     * @return lowercase hyphen-separated slug
     */
    public String toSlug(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Slug input must not be blank");
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = normalized
                .toLowerCase(Locale.ENGLISH);

        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        slug = MULTI_DASH.matcher(slug).replaceAll("-");
        slug = LEADING_DASH.matcher(slug).replaceAll("");

        if (slug.isBlank()) {
            throw new IllegalArgumentException("Title produces an empty slug: " + input);
        }

        return slug;
    }

    /**
     * Append a numeric suffix to make a slug unique.
     * Example: "two-sum" + 2 → "two-sum-2"
     */
    public String toUniqueSlug(String input, long suffix) {
        return toSlug(input) + "-" + suffix;
    }
}
