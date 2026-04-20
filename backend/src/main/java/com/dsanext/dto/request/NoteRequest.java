package com.dsanext.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoteRequest {

    @NotBlank(message = "Note content is required")
    @Size(min = 1, max = 50000, message = "Note content must not exceed 50000 characters")
    private String content;
}
