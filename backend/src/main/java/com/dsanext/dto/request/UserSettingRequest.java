package com.dsanext.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSettingRequest {

    @Pattern(regexp = "^(light|dark|system)$", message = "Theme must be light, dark, or system")
    private String theme;

    private Boolean notificationsEnabled;

    private Boolean emailNotifications;

    @Size(max = 100, message = "LeetCode username must not exceed 100 characters")
    private String lcUsername;

    @Size(max = 100, message = "Codeforces username must not exceed 100 characters")
    private String cfUsername;

    @Size(max = 100, message = "HackerRank username must not exceed 100 characters")
    private String hrUsername;

    @Size(max = 100, message = "InterviewBit username must not exceed 100 characters")
    private String ibUsername;
}
