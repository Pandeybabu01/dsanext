//package com.dsanext.domain.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
///**
// * Coding platform entity — LeetCode, Codeforces, HackerRank, InterviewBit, etc.
// */
//@Entity
//@Table(name = "platforms")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Platform extends BaseEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(nullable = false, updatable = false)
//    private UUID id;
//
//    @Column(nullable = false, unique = true, length = 100)
//    private String name;
//
//    @Column(name = "base_url", nullable = false, length = 500)
//    private String baseUrl;
//
//    @Column(name = "icon_url", length = 500)
//    private String iconUrl;
//
//    @Column(name = "is_active", nullable = false)
//    @Builder.Default
//    private boolean isActive = true;
//
//    // ── Relationships ────────────────────────────────────────
//
//    @OneToMany(mappedBy = "platform", fetch = FetchType.LAZY)
//    @Builder.Default
//    private List<Problem> problems = new ArrayList<>();
//}




package com.dsanext.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // ✅ ADD THIS
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Coding platform entity — LeetCode, Codeforces, HackerRank, InterviewBit, etc.
 */
@Entity
@Table(name = "platforms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Platform extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // ── Relationships ────────────────────────────────────────

    @OneToMany(mappedBy = "platform", fetch = FetchType.LAZY)
    @JsonIgnore // ✅ IMPORTANT FIX (prevents infinite recursion / lazy loading issue)
    @Builder.Default
    private List<Problem> problems = new ArrayList<>();
}