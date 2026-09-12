package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.MilestoneStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MilestoneResponseDTO {

    private Long id;
    private String title;
    private String description;
    private MilestoneStatus status;
    private LocalDateTime dueDate;

    // Project ki complete entity nahi bhejenge
    private Long projectId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}