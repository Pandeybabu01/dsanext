package com.dsanext.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlatformRequest {

    @NotBlank(message = "Platform name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Base URL is required")
    @Pattern(regexp = "^https?://.*", message = "Base URL must be a valid HTTP/HTTPS URL")
    @Size(max = 500, message = "Base URL must not exceed 500 characters")
    private String baseUrl;

    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    private Boolean isActive = true;
}
