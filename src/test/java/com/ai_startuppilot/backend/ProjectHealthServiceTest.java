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

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    // ===== Helper to setup common mocks =====
    private void mockEmptyMilestonesAndRisks(Long projectId) {
        when(milestoneRepository.findByProjectId(eq(projectId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        when(riskRepository.findByProjectId(eq(projectId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
    }

    private Project makeProject(Long id, String name) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        return project;
    }

    private Task makeTask(TaskStatus status, boolean overdue) {
        Task task = new Task();
        task.setStatus(status);
        if (overdue) {
            task.setDueDate(LocalDateTime.now().minusDays(2));
        } else {
            task.setDueDate(LocalDateTime.now().plusDays(2));
        }
        return task;
    }

    // ===== Zero Tasks =====
    @Test
    void testProjectHealth_WithZeroTasks_ShouldHaveZeroOverdueAnd100PercentCompletion() {
        Project project = makeProject(1L, "Empty Project");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));
        mockEmptyMilestonesAndRisks(1L);

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);

        assertEquals(0, health.getOverdueTasks(), "Overdue tasks should be zero");
        assertEquals(100.0, health.getTaskCompletionRate(), "Completion rate for 0 tasks should be 100%");
        assertTrue(health.getWarnings().stream().noneMatch(w -> w.contains("overdue")),
                "Should not have overdue warnings when no tasks");
    }

    // ===== All Completed =====
    @Test
    void testProjectHealth_WithAllCompletedTasks_ShouldHave100PercentCompletion() {
        Project project = makeProject(1L, "Full Project");
        Task t1 = makeTask(TaskStatus.COMPLETED, false);
        Task t2 = makeTask(TaskStatus.COMPLETED, false);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(t1, t2)));
        mockEmptyMilestonesAndRisks(1L);

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);

        assertEquals(100.0, health.getTaskCompletionRate(), "Completion rate should be 100%");
        assertEquals(0, health.getOverdueTasks(), "Overdue tasks should be 0");
        assertTrue(health.getWarnings().stream().noneMatch(w -> w.contains("overdue")),
                "No overdue warning when all tasks completed");
    }

    // ===== Partial Completion (50%) =====
    @Test
    void testProjectHealth_WithSomeCompletedTasks_ShouldCalculateCorrectPercentage() {
        Project project = makeProject(1L, "Partial Project");
        Task completed = makeTask(TaskStatus.COMPLETED, false);
        Task todo = makeTask(TaskStatus.TODO, false);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(completed, todo)));
        mockEmptyMilestonesAndRisks(1L);

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);

        assertEquals(50.0, health.getTaskCompletionRate(), "Completion rate should be 50%");
    }

    // ===== No Completed Tasks =====
    @Test
    void testProjectHealth_WithNoCompletedTasks_ShouldHave0PercentCompletion() {
        Project project = makeProject(1L, "No Progress Project");
        Task t1 = makeTask(TaskStatus.TODO, false);
        Task t2 = makeTask(TaskStatus.IN_PROGRESS, false);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(t1, t2)));
        mockEmptyMilestonesAndRisks(1L);

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);

        assertEquals(0.0, health.getTaskCompletionRate(), "Completion rate should be 0%");
        assertTrue(health.getWarnings().stream().anyMatch(w -> w.contains("50%")),
                "Should have warning about completion rate below 50%");
    }

    // ===== Overdue tasks affect health =====
    @Test
    void testProjectHealth_WithOverdueTasks_ShouldReportOverdueAndReduceScore() {
        Project project = makeProject(1L, "Overdue Project");
        Task overdue = makeTask(TaskStatus.TODO, true);
        Task onTime  = makeTask(TaskStatus.TODO, false);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(overdue, onTime)));
        mockEmptyMilestonesAndRisks(1L);

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);

        assertEquals(1, health.getOverdueTasks(), "Should report 1 overdue task");
        assertTrue(health.getWarnings().stream().anyMatch(w -> w.contains("overdue")),
                "Should have overdue warning");
    }

    // ===== Zero overdue tasks should NOT produce overdue warning =====
    @Test
    void testProjectHealth_WithZeroOverdueTasks_ShouldNotProduceOverdueWarning() {
        Project project = makeProject(1L, "On-Time Project");
        Task t1 = makeTask(TaskStatus.IN_PROGRESS, false);
        Task t2 = makeTask(TaskStatus.TODO, false);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(t1, t2)));
        mockEmptyMilestonesAndRisks(1L);

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);

        assertEquals(0, health.getOverdueTasks());
        assertTrue(health.getWarnings().stream().noneMatch(w -> w.contains("overdue")),
                "Zero overdue tasks should not produce an overdue warning");
    }

    // ===== Mixed states (TODO, IN_PROGRESS, BLOCKED, COMPLETED) =====
    @Test
    void testProjectHealth_WithMixedTaskStates_ShouldOnlyCountCompleted() {
        Project project = makeProject(1L, "Mixed Project");
        Task completed  = makeTask(TaskStatus.COMPLETED, false);
        Task todo       = makeTask(TaskStatus.TODO, false);
        Task inProgress = makeTask(TaskStatus.IN_PROGRESS, false);
        Task blocked    = makeTask(TaskStatus.BLOCKED, false);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(completed, todo, inProgress, blocked)));
        mockEmptyMilestonesAndRisks(1L);

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);

        assertEquals(25.0, health.getTaskCompletionRate(), "Only COMPLETED tasks count toward rate");
    }

    // ===== Project Not Found =====
    @Test
    void testProjectHealth_WhenProjectNotFound_ShouldThrowException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                com.ai_startuppilot.backend.exception.ProjectNotFoundException.class,
                () -> projectHealthService.getProjectHealth(999L),
                "Should throw ProjectNotFoundException for non-existent project"
        );
    }

    // ===== Health Status threshold =====
    @Test
    void testProjectHealth_WithHighCompletion_ShouldBeHealthy() {
        Project project = makeProject(1L, "Healthy Project");
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(makeTask(TaskStatus.COMPLETED, false));
        }
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(tasks));
        mockEmptyMilestonesAndRisks(1L);

        ProjectHealthResponseDTO health = projectHealthService.getProjectHealth(1L);

        assertEquals("HEALTHY", health.getHealthStatus(), "Project with high completion should be HEALTHY");
        assertTrue(health.getHealthScore() >= 80, "Health score should be >= 80 for healthy project");
    }
}
