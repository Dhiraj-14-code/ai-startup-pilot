package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.MilestoneStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MilestoneRequestDTO {

    @NotBlank(message = "Milestone title is required")
    @Size(min = 3, max = 150,
            message = "Milestone title must be between 3 and 150 characters")
    private String title;

    @Size(max = 1000,
            message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Milestone status is required")
    private MilestoneStatus status;

    private LocalDateTime dueDate;

    @NotNull(message = "Project ID is required")
    private Long projectId;
}