package com.dsanext.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAppSettingRequest {

    @NotBlank(message = "Setting value is required")
    @Size(max = 5000, message = "Setting value must not exceed 5000 characters")
    private String value;
}
