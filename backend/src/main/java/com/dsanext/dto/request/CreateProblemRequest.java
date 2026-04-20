package com.dsanext.dto.request;

import com.dsanext.domain.enums.Difficulty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateProblemRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 300, message = "Title must be between 3 and 300 characters")
    private String title;

    @Size(max = 50000, message = "Description must not exceed 50000 characters")
    private String description;

    @NotBlank(message = "Topic is required")
    @Size(min = 2, max = 100, message = "Topic must be between 2 and 100 characters")
    private String topic;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Size(max = 500, message = "External URL must not exceed 500 characters")
    @Pattern(
        regexp = "^$|^https?://.*",
        message = "External URL must be a valid HTTP/HTTPS URL"
    )
    private String externalUrl;

    private UUID platformId;
}
