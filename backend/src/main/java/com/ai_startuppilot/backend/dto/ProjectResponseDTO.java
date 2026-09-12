package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.ProjectStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class ProjectResponseDTO {
    private Long id;

    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



}

