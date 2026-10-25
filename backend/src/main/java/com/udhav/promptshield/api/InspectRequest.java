package com.udhav.promptshield.api;

import com.udhav.promptshield.domain.ProviderMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InspectRequest(
        @NotBlank(message = "prompt is required")
        @Size(min = 5, max = 6000, message = "prompt must be between 5 and 6000 characters")
        String prompt,
        @NotNull(message = "mode is required")
        ProviderMode mode
) {
}
