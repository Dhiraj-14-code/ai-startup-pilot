package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.TaskPriority;
import com.ai_startuppilot.backend.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequestDTO {

    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 150,
            message = "Task title must be between 3 and 150 characters")
    private String title;

    @Size(max = 1000,
            message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Task status is required")
    private TaskStatus status;

    @NotNull(message = "Task priority is required")
    private TaskPriority priority;

    private LocalDateTime dueDate;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assignedUserId;
}