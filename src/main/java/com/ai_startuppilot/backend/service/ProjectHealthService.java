package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.ProjectAIRequestDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectHealthService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final RiskRepository riskRepository;
    private final RestClient restClient;

    public ProjectHealthService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            MilestoneRepository milestoneRepository,
            RiskRepository riskRepository,
            @Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
        this.riskRepository = riskRepository;
        this.restClient = RestClient.builder().baseUrl(aiServiceUrl).build();
    }

    public ProjectHealthResponseDTO getProjectHealth(Long projectId) {

        // Project exist karta hai ya nahi check karo
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project ID " + projectId + " not found"));

        // Project ke related data ko fetch karo
        List<Task> tasks =
                taskRepository.findByProjectId(projectId, Pageable.unpaged()).getContent();

        List<Milestone> milestones =
                milestoneRepository.findByProjectId(projectId, Pageable.unpaged()).getContent();

        List<Risk> risks =
                riskRepository.findByProjectId(projectId, Pageable.unpaged()).getContent();

        try {
            return getProjectHealthFromAI(project, tasks, milestones, risks);
        } catch (Exception e) {
            System.err.println("AI Service failed, falling back to deterministic calculation: " + e.getMessage());
            return calculateDeterministicHealth(project, tasks, milestones, risks);
        }
    }

    private ProjectHealthResponseDTO getProjectHealthFromAI(
            Project project, List<Task> tasks, List<Milestone> milestones, List<Risk> risks) {
        
        ProjectAIRequestDTO requestDTO = new ProjectAIRequestDTO();
        requestDTO.setProjectId(project.getId());
        requestDTO.setProjectName(project.getName());
        
        requestDTO.setTasks(tasks.stream().map(t -> {
            ProjectAIRequestDTO.TaskDTO dto = new ProjectAIRequestDTO.TaskDTO();
            dto.setId(t.getId());
            dto.setTitle(t.getTitle());
            dto.setStatus(t.getStatus().name());
            dto.setPriority(t.getPriority().name());
            dto.setDueDate(t.getDueDate());
            if (t.getAssignedUser() != null) {
                dto.setAssignedUserId(t.getAssignedUser().getId());
                dto.setAssignedUserName(t.getAssignedUser().getName());
            }
            return dto;
        }).collect(Collectors.toList()));
        
        requestDTO.setMilestones(milestones.stream().map(m -> {
            ProjectAIRequestDTO.MilestoneDTO dto = new ProjectAIRequestDTO.MilestoneDTO();
            dto.setId(m.getId());
            dto.setTitle(m.getTitle());
            dto.setStatus(m.getStatus().name());
            dto.setDueDate(m.getDueDate());
            return dto;
        }).collect(Collectors.toList()));
        
        requestDTO.setRisks(risks.stream().map(r -> {
            ProjectAIRequestDTO.RiskDTO dto = new ProjectAIRequestDTO.RiskDTO();
            dto.setId(r.getId());
            dto.setTitle(r.getTitle());
            dto.setSeverity(r.getSeverity().name());
            dto.setStatus(r.getStatus().name());
            return dto;
        }).collect(Collectors.toList()));
        
        ResponseEntity<ProjectHealthResponseDTO> response = restClient.post()
                .uri("/api/v1/analyze/project")
                .body(requestDTO)
                .retrieve()
                .toEntity(ProjectHealthResponseDTO.class);
                
        return response.getBody();
    }

    private ProjectHealthResponseDTO calculateDeterministicHealth(
            Project project, List<Task> tasks, List<Milestone> milestones, List<Risk> risks) {
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
        
        response.setPrediction("[RULE_BASED] Fallback prediction.");
        response.setInsights(List.of("[RULE_BASED] Computed deterministically due to AI service unavailability."));

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