package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.RiskSeverity;
import com.ai_startuppilot.backend.enums.RiskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskRequestDTO {

    @NotBlank(message = "Risk title is required")
    @Size(min = 3, max = 150,
            message = "Risk title must be between 3 and 150 characters")
    private String title;

    @Size(max = 1000,
            message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Risk severity is required")
    private RiskSeverity severity;

    @NotNull(message = "Risk status is required")
    private RiskStatus status;

    @NotNull(message = "Project ID is required")
    private Long projectId;
}