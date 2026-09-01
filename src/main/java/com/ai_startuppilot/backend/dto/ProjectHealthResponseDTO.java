package com.ai_startuppilot.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectHealthResponseDTO {

    private Long projectId;
    private String projectName;

    private double healthScore;
    private String healthStatus;

    private double taskCompletionRate;
    private int overdueTasks;

    private int totalMilestones;
    private int completedMilestones;

    private int openRisks;
    private int criticalRisks;

    private List<String> warnings;
    private List<String> recommendations;

    private double milestoneProgress;
    private int highRisks;
    private int mediumRisks;
    private int lowRisks;
}