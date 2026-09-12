package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequestDTO {

    @NotBlank(message = "Project name is required")

    @Size(min = 3, max = 100,
            message = "Project name must be between 3 and 100 characters"
        )
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Status is required")
    private ProjectStatus status;


}