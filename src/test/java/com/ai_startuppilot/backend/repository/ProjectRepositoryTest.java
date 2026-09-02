package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Milestone;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Risk;
import com.ai_startuppilot.backend.entity.Task;
import com.ai_startuppilot.backend.enums.MilestoneStatus;
import com.ai_startuppilot.backend.enums.ProjectStatus;
import com.ai_startuppilot.backend.enums.RiskSeverity;
import com.ai_startuppilot.backend.enums.RiskStatus;
import com.ai_startuppilot.backend.enums.TaskPriority;
import com.ai_startuppilot.backend.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository integration tests using H2 in-memory database.
 * Verifies JPA relationships, cascading deletes, and pagination queries.
 * Uses @SpringBootTest since @DataJpaTest does not exist in Spring Boot 4.x.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RiskRepository riskRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    // ===== Project Persists =====
    @Test
    void saveProject_ShouldPersistWithGeneratedId() {
        Project project = new Project();
        project.setName("Alpha Startup");
        project.setStatus(ProjectStatus.ACTIVE);

        Project saved = projectRepository.save(project);

        assertNotNull(saved.getId(), "Generated ID should not be null");
        assertEquals("Alpha Startup", saved.getName());
        assertEquals(ProjectStatus.ACTIVE, saved.getStatus());
    }

    // ===== Task belongs to Project =====
    @Test
    void saveTask_WithProject_ShouldBeQueryableByProjectId() {
        Project project = new Project();
        project.setName("Task Parent Project");
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("Fix Login Bug");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setProject(savedProject);
        taskRepository.save(task);

        Page<Task> tasks = taskRepository.findByProjectId(savedProject.getId(), PageRequest.of(0, 10));

        assertEquals(1, tasks.getTotalElements());
        assertEquals("Fix Login Bug", tasks.getContent().get(0).getTitle());
    }

    // ===== Risk belongs to Project =====
    @Test
    void saveRisk_WithProject_ShouldBeQueryableByProjectId() {
        Project project = new Project();
        project.setName("Risk Parent Project");
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        Risk risk = new Risk();
        risk.setTitle("Data Breach");
        risk.setSeverity(RiskSeverity.CRITICAL);
        risk.setStatus(RiskStatus.OPEN);
        risk.setProject(savedProject);
        riskRepository.save(risk);

        Page<Risk> risks = riskRepository.findByProjectId(savedProject.getId(), PageRequest.of(0, 10));

        assertEquals(1, risks.getTotalElements());
        assertEquals(RiskSeverity.CRITICAL, risks.getContent().get(0).getSeverity());
    }

    // ===== Milestone belongs to Project =====
    @Test
    void saveMilestone_WithProject_ShouldBeQueryableByProjectId() {
        Project project = new Project();
        project.setName("Milestone Parent Project");
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        Milestone milestone = new Milestone();
        milestone.setTitle("MVP Launch");
        milestone.setStatus(MilestoneStatus.IN_PROGRESS);
        milestone.setProject(savedProject);
        milestoneRepository.save(milestone);

        Page<Milestone> milestones = milestoneRepository.findByProjectId(savedProject.getId(), PageRequest.of(0, 10));

        assertEquals(1, milestones.getTotalElements());
        assertEquals("MVP Launch", milestones.getContent().get(0).getTitle());
    }

    // ===== Cascade Delete: Tasks deleted with Project =====
    @Test
    void deleteProject_ShouldCascadeDeleteTasks() {
        Project project = new Project();
        project.setName("Cascade Test Project");
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("Cascade Task");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(savedProject);
        Task savedTask = taskRepository.save(task);

        entityManager.flush();
        entityManager.clear();

        projectRepository.deleteById(savedProject.getId());
        entityManager.flush();

        Optional<Task> deletedTask = taskRepository.findById(savedTask.getId());
        assertTrue(deletedTask.isEmpty(), "Task should be deleted when parent project is deleted");
    }

    // ===== Cascade Delete: Risks deleted with Project =====
    @Test
    void deleteProject_ShouldCascadeDeleteRisks() {
        Project project = new Project();
        project.setName("Cascade Risk Project");
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        Risk risk = new Risk();
        risk.setTitle("Cascade Risk");
        risk.setSeverity(RiskSeverity.LOW);
        risk.setStatus(RiskStatus.OPEN);
        risk.setProject(savedProject);
        Risk savedRisk = riskRepository.save(risk);

        entityManager.flush();
        entityManager.clear();

        projectRepository.deleteById(savedProject.getId());
        entityManager.flush();

        Optional<Risk> deletedRisk = riskRepository.findById(savedRisk.getId());
        assertTrue(deletedRisk.isEmpty(), "Risk should be deleted when parent project is deleted");
    }

    // ===== Cascade Delete: Milestones deleted with Project =====
    @Test
    void deleteProject_ShouldCascadeDeleteMilestones() {
        Project project = new Project();
        project.setName("Cascade Milestone Project");
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        Milestone milestone = new Milestone();
        milestone.setTitle("Cascade Milestone");
        milestone.setStatus(MilestoneStatus.NOT_STARTED);
        milestone.setProject(savedProject);
        Milestone savedMilestone = milestoneRepository.save(milestone);

        entityManager.flush();
        entityManager.clear();

        projectRepository.deleteById(savedProject.getId());
        entityManager.flush();

        Optional<Milestone> deletedMilestone = milestoneRepository.findById(savedMilestone.getId());
        assertTrue(deletedMilestone.isEmpty(), "Milestone should be deleted when parent project is deleted");
    }

    // ===== Pagination: empty page beyond data =====
    @Test
    void taskPagination_PageBeyondData_ShouldReturnEmptyContent() {
        Project project = new Project();
        project.setName("Pagination Project");
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("Only Task");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.LOW);
        task.setProject(savedProject);
        taskRepository.save(task);

        // Request page 5 when only 1 item exists
        Page<Task> emptyPage = taskRepository.findByProjectId(savedProject.getId(), PageRequest.of(5, 10));

        assertEquals(0, emptyPage.getContent().size(), "Page beyond data should have empty content");
        assertEquals(1, emptyPage.getTotalElements(), "Total elements should still be 1");
    }

    // ===== Pagination: correct page size and total pages =====
    @Test
    void taskPagination_ShouldRespectPageSizeAndReturnCorrectTotals() {
        Project project = new Project();
        project.setName("Page Size Project");
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);

        for (int i = 1; i <= 7; i++) {
            Task task = new Task();
            task.setTitle("Task " + i);
            task.setStatus(TaskStatus.TODO);
            task.setPriority(TaskPriority.LOW);
            task.setProject(savedProject);
            taskRepository.save(task);
        }

        Page<Task> page = taskRepository.findByProjectId(savedProject.getId(), PageRequest.of(0, 5));

        assertEquals(5, page.getContent().size(), "Page size should be 5");
        assertEquals(7, page.getTotalElements(), "Total elements should be 7");
        assertEquals(2, page.getTotalPages(), "Total pages should be 2 (ceil(7/5))");
    }
}
