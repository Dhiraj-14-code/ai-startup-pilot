package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.ProjectHealthResponseDTO;
import com.ai_startuppilot.backend.entity.Milestone;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Risk;
import com.ai_startuppilot.backend.entity.Task;
import com.ai_startuppilot.backend.enums.MilestoneStatus;
import com.ai_startuppilot.backend.enums.RiskSeverity;
import com.ai_startuppilot.backend.enums.RiskStatus;
import com.ai_startuppilot.backend.enums.TaskStatus;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.repository.MilestoneRepository;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import com.ai_startuppilot.backend.repository.RiskRepository;
import com.ai_startuppilot.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectHealthService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final RiskRepository riskRepository;

    public ProjectHealthService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            MilestoneRepository milestoneRepository,
            RiskRepository riskRepository) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
        this.riskRepository = riskRepository;
    }

    public ProjectHealthResponseDTO getProjectHealth(Long projectId) {

        // Project exist karta hai ya nahi check karo
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project ID " + projectId + " not found"));

        // Project ke related data ko fetch karo
        List<Task> tasks =
                taskRepository.findByProjectId(projectId);

        List<Milestone> milestones =
                milestoneRepository.findByProjectId(projectId);

        List<Risk> risks =
                riskRepository.findByProjectId(projectId);

        // Metrics calculate karo
        int totalTasks = tasks.size();

        int completedTasks = (int) tasks.stream()
                .filter(task ->
                        task.getStatus() == TaskStatus.COMPLETED)
                .count();

        int overdueTasks = (int) tasks.stream()
                .filter(Task::isOverdue)
                .count();

        int totalMilestones = milestones.size();

        int completedMilestones = (int) milestones.stream()
                .filter(milestone ->
                        milestone.getStatus() == MilestoneStatus.COMPLETED)
                .count();

        int openRisks = (int) risks.stream()
                .filter(risk ->
                        risk.getStatus() != RiskStatus.CLOSED)
                .count();

        int criticalRisks = (int) risks.stream()
                .filter(risk ->
                        risk.getSeverity() == RiskSeverity.CRITICAL
                                && risk.getStatus() != RiskStatus.CLOSED)
                .count();

        // Completion rates calculate karo
        double taskCompletionRate =
                calculatePercentage(completedTasks, totalTasks);

        double milestoneProgress =
                calculatePercentage(
                        completedMilestones,
                        totalMilestones);

        // Health score calculate karo
        double healthScore = calculateHealthScore(
                taskCompletionRate,
                overdueTasks,
                milestoneProgress,
                openRisks,
                criticalRisks
        );

        // Status determine karo
        String healthStatus =
                determineHealthStatus(healthScore);

        // Warnings and recommendations
        List<String> warnings =
                generateWarnings(
                        overdueTasks,
                        criticalRisks,
                        openRisks,
                        taskCompletionRate,
                        milestoneProgress
                );

        List<String> recommendations =
                generateRecommendations(
                        overdueTasks,
                        criticalRisks,
                        openRisks,
                        milestoneProgress
                );

        // Response DTO
        ProjectHealthResponseDTO response =
                new ProjectHealthResponseDTO();

        response.setProjectId(project.getId());
        response.setProjectName(project.getName());

        response.setHealthScore(healthScore);
        response.setHealthStatus(healthStatus);

        response.setTaskCompletionRate(taskCompletionRate);
        response.setOverdueTasks(overdueTasks);

        response.setTotalMilestones(totalMilestones);
        response.setCompletedMilestones(completedMilestones);

        response.setOpenRisks(openRisks);
        response.setCriticalRisks(criticalRisks);

        response.setWarnings(warnings);
        response.setRecommendations(recommendations);

        return response;
    }

    // Percentage calculate karne ka common method
    private double calculatePercentage(
            int completed,
            int total) {

        if (total == 0) {
            return 100.0;
        }

        return ((double) completed / total) * 100;
    }


    // Health score calculation
    private double calculateHealthScore(
            double taskCompletionRate,
            int overdueTasks,
            double milestoneProgress,
            int openRisks,
            int criticalRisks) {

        // Task completion = 40%
        double taskScore =
                taskCompletionRate * 0.40;

        // Milestone progress = 20%
        double milestoneScore =
                milestoneProgress * 0.20;

        // Overdue task penalty
        double overduePenalty =
                Math.min(overdueTasks * 5, 20);

        // Risk penalty
        double riskPenalty =
                Math.min(
                        (openRisks * 3) +
                                (criticalRisks * 7),
                        20
                );

        double score =
                taskScore
                        + milestoneScore
                        + 20
                        - overduePenalty
                        - riskPenalty;

        return Math.max(0, Math.min(100, score));
    }


    // Score ke according project status
    private String determineHealthStatus(
            double healthScore) {

        if (healthScore >= 80) {
            return "HEALTHY";
        }

        if (healthScore >= 60) {
            return "AT_RISK";
        }

        if (healthScore >= 40) {
            return "CRITICAL";
        }

        return "SEVERELY_CRITICAL";
    }

    // Project warnings generate karo
    private List<String> generateWarnings(
            int overdueTasks,
            int criticalRisks,
            int openRisks,
            double taskCompletionRate,
            double milestoneProgress) {

        List<String> warnings = new ArrayList<>();

        if (overdueTasks > 0) {
            warnings.add(
                    overdueTasks +
                            " task(s) are overdue");
        }

        if (criticalRisks > 0) {
            warnings.add(
                    criticalRisks +
                            " critical risk(s) are open");
        }

        if (openRisks >= 3) {
            warnings.add(
                    "Project has multiple open risks");
        }

        if (taskCompletionRate < 50) {
            warnings.add(
                    "Task completion rate is below 50%");
        }

        if (milestoneProgress < 50) {
            warnings.add(
                    "Milestone progress is below 50%");
        }

        return warnings;
    }

    // Recommended actions generate karo
    private List<String> generateRecommendations(
            int overdueTasks,
            int criticalRisks,
            int openRisks,
            double milestoneProgress) {

        List<String> recommendations =
                new ArrayList<>();

        if (overdueTasks > 0) {
            recommendations.add(
                    "Prioritize overdue tasks");
        }

        if (criticalRisks > 0) {
            recommendations.add(
                    "Resolve critical risks first");
        }

        if (openRisks >= 3) {
            recommendations.add(
                    "Review and prioritize open risks");
        }

        if (milestoneProgress < 50) {
            recommendations.add(
                    "Review milestone progress and deadlines");
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                    "Project is progressing normally");
        }

        return recommendations;
    }

}