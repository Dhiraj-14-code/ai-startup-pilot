package com.ai_startuppilot.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Data
public class ProjectAIRequestDTO {
    private Long projectId;
    private String projectName;
    private List<TaskDTO> tasks;
    private List<MilestoneDTO> milestones;
    private List<RiskDTO> risks;

    @Data
    public static class TaskDTO {
        private Long id;
        private String title;
        private String status;
        private String priority;
        private LocalDateTime dueDate;
        private Long assignedUserId;
        private String assignedUserName;
    }

    @Data
    public static class MilestoneDTO {
        private Long id;
        private String title;
        private String status;
        private LocalDateTime dueDate;
    }

    @Data
    public static class RiskDTO {
        private Long id;
        private String title;
        private String severity;
        private String status;
    }
}
