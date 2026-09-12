package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.TaskPriority;
import com.ai_startuppilot.backend.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskResponseDTO {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDateTime dueDate;

    private Long projectId;

    private Long assignedUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}