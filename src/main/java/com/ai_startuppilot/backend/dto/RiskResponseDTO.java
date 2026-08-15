package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.RiskSeverity;
import com.ai_startuppilot.backend.enums.RiskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RiskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private RiskSeverity severity;
    private RiskStatus status;

    // Sirf project ID response mein bhejenge
    private Long projectId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}