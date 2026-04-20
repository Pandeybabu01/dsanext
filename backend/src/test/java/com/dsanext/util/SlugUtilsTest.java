package com.dsanext.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SlugUtils Unit Tests")
class SlugUtilsTest {

    private final SlugUtils slugUtils = new SlugUtils();

    @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
    @CsvSource({
        "Two Sum,              two-sum",
        "Binary Tree Level Order Traversal, binary-tree-level-order-traversal",
        "Add Two Numbers,      add-two-numbers",
        "Merge K Sorted Lists, merge-k-sorted-lists",
        "Valid Parentheses,    valid-parentheses",
        "N-Queens II,          n-queens-ii",
        "Longest Substring Without Repeating Characters, longest-substring-without-repeating-characters",
    })
    @DisplayName("toSlug — converts titles to URL-safe slugs")
    void toSlug_variousTitles(String input, String expected) {
        assertThat(slugUtils.toSlug(input.trim())).isEqualTo(expected.trim());
    }

    @Test
    @DisplayName("toSlug — removes special characters")
    void toSlug_removesSpecialChars() {
        assertThat(slugUtils.toSlug("C++ Vector")).isEqualTo("c-vector");
        assertThat(slugUtils.toSlug("O(n log n)")).isEqualTo("on-log-n");
    }

    @Test
    @DisplayName("toSlug — collapses multiple spaces and hyphens")
    void toSlug_collapsesMultipleSpaces() {
        assertThat(slugUtils.toSlug("Two   Sum")).isEqualTo("two-sum");
    }

    @Test
    @DisplayName("toSlug — lowercases output")
    void toSlug_lowercases() {
        assertThat(slugUtils.toSlug("BINARY SEARCH")).isEqualTo("binary-search");
        assertThat(slugUtils.toSlug("Dynamic Programming")).isEqualTo("dynamic-programming");
    }

    @Test
    @DisplayName("toSlug — null input throws IllegalArgumentException")
    void toSlug_nullInput_throwsException() {
        assertThatThrownBy(() -> slugUtils.toSlug(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toSlug — blank input throws IllegalArgumentException")
    void toSlug_blankInput_throwsException() {
        assertThatThrownBy(() -> slugUtils.toSlug(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slugUtils.toSlug("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("toUniqueSlug — appends numeric suffix")
    void toUniqueSlug_appendsSuffix() {
        assertThat(slugUtils.toUniqueSlug("Two Sum", 2)).isEqualTo("two-sum-2");
        assertThat(slugUtils.toUniqueSlug("Two Sum", 10)).isEqualTo("two-sum-10");
    }
}
