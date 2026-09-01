package com.ai_startuppilot.backend;

import com.ai_startuppilot.backend.dto.ProjectHealthResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Task;
import com.ai_startuppilot.backend.enums.TaskStatus;
import com.ai_startuppilot.backend.repository.MilestoneRepository;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import com.ai_startuppilot.backend.repository.RiskRepository;
import com.ai_startuppilot.backend.repository.TaskRepository;
import com.ai_startuppilot.backend.service.ProjectHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ProjectHealthServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private RiskRepository riskRepository;

    @InjectMocks
    private ProjectHealthService projectHealthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProjectHealth_WithZeroTasks_ShouldHaveZeroOverdueAnd100PercentCompletion() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Empty Project");
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(1L)).thenReturn(new ArrayList<>());
        when(milestoneRepository.findByProjectId(1L)).thenReturn(new ArrayList<>());
        when(riskRepository.findByProjectId(1L)).thenReturn(new ArrayList<>());

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);
        
        assertEquals(0, health.getOverdueTasks(), "Overdue tasks should be zero");
        assertEquals(100.0, health.getTaskCompletionRate(), "Completion rate for 0 tasks should be 100%");
        assertTrue(health.getWarnings().stream().noneMatch(w -> w.contains("overdue")), "Should not have overdue warnings");
    }

    @Test
    void testProjectHealth_WithAllCompletedTasks_ShouldHave100PercentCompletion() {
        Project project = new Project();
        project.setId(1L);
        
        Task task1 = new Task();
        task1.setStatus(TaskStatus.COMPLETED);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(task1));
        when(milestoneRepository.findByProjectId(1L)).thenReturn(new ArrayList<>());
        when(riskRepository.findByProjectId(1L)).thenReturn(new ArrayList<>());

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);
        
        assertEquals(100.0, health.getTaskCompletionRate(), "Completion rate should be 100%");
        assertEquals(0, health.getOverdueTasks(), "Overdue tasks should be 0");
    }

    @Test
    void testProjectHealth_WithSomeCompletedTasks_ShouldCalculateCorrectPercentage() {
        Project project = new Project();
        project.setId(1L);
        
        Task task1 = new Task();
        task1.setStatus(TaskStatus.COMPLETED);
        
        Task task2 = new Task();
        task2.setStatus(TaskStatus.TODO);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(task1, task2));
        when(milestoneRepository.findByProjectId(1L)).thenReturn(new ArrayList<>());
        when(riskRepository.findByProjectId(1L)).thenReturn(new ArrayList<>());

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);
        
        assertEquals(50.0, health.getTaskCompletionRate(), "Completion rate should be 50%");
    }
}
