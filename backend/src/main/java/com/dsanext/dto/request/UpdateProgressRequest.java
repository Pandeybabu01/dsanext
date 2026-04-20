package com.dsanext.dto.request;

import com.dsanext.domain.enums.ProgressStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProgressRequest {

    @NotNull(message = "Status is required")
    private ProgressStatus status;
}
