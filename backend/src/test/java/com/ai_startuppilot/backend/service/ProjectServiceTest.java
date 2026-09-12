package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.ProjectRequestDTO;
import com.ai_startuppilot.backend.dto.ProjectResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.enums.ProjectStatus;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.mapper.ProjectMapper;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===== Create Project =====
    @Test
    void createProject_ShouldReturnProjectResponseDTO() {
        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("Startup Alpha");
        request.setStatus(ProjectStatus.ACTIVE);

        Project entity = new Project();
        entity.setId(1L);
        entity.setName("Startup Alpha");
        entity.setStatus(ProjectStatus.ACTIVE);

        ProjectResponseDTO response = new ProjectResponseDTO();
        response.setId(1L);
        response.setName("Startup Alpha");

        when(projectMapper.mapToEntity(request)).thenReturn(entity);
        when(projectRepository.save(entity)).thenReturn(entity);
        when(projectMapper.mapToDto(entity)).thenReturn(response);

        ProjectResponseDTO result = projectService.createProject(request);

        assertEquals("Startup Alpha", result.getName());
        assertEquals(1L, result.getId());
        verify(projectRepository, times(1)).save(entity);
    }

    // ===== Get All Projects =====
    @Test
    void getAllProjects_ShouldReturnListOfProjects() {
        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("Alpha");

        Project p2 = new Project();
        p2.setId(2L);
        p2.setName("Beta");

        ProjectResponseDTO dto1 = new ProjectResponseDTO();
        dto1.setId(1L);
        dto1.setName("Alpha");

        ProjectResponseDTO dto2 = new ProjectResponseDTO();
        dto2.setId(2L);
        dto2.setName("Beta");

        when(projectRepository.findAll()).thenReturn(List.of(p1, p2));
        when(projectMapper.mapToDto(p1)).thenReturn(dto1);
        when(projectMapper.mapToDto(p2)).thenReturn(dto2);

        List<ProjectResponseDTO> result = projectService.getAllProjects();

        assertEquals(2, result.size());
        assertEquals("Alpha", result.get(0).getName());
        assertEquals("Beta", result.get(1).getName());
    }

    // ===== Get Project by ID - Found =====
    @Test
    void getProjectByID_WhenProjectExists_ShouldReturnProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Alpha");

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(1L);
        dto.setName("Alpha");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.mapToDto(project)).thenReturn(dto);

        ProjectResponseDTO result = projectService.getProjectByID(1L);

        assertEquals(1L, result.getId());
        assertEquals("Alpha", result.getName());
    }

    // ===== Get Project by ID - Not Found =====
    @Test
    void getProjectByID_WhenProjectNotFound_ShouldThrowException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> projectService.getProjectByID(99L),
                "Should throw ProjectNotFoundException for non-existent ID");
    }

    // ===== Update Project =====
    @Test
    void updateProject_WhenProjectExists_ShouldUpdateAndReturn() {
        Project existing = new Project();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setStatus(ProjectStatus.PLANNING);

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("New Name");
        request.setDescription("Updated desc");
        request.setStatus(ProjectStatus.ACTIVE);

        Project updated = new Project();
        updated.setId(1L);
        updated.setName("New Name");
        updated.setStatus(ProjectStatus.ACTIVE);

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(1L);
        dto.setName("New Name");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(updated);
        when(projectMapper.mapToDto(updated)).thenReturn(dto);

        ProjectResponseDTO result = projectService.updateProject(1L, request);

        assertEquals("New Name", result.getName());
        verify(projectRepository, times(1)).save(existing);
    }

    // ===== Update Project - Not Found =====
    @Test
    void updateProject_WhenProjectNotFound_ShouldThrowException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("X");
        request.setStatus(ProjectStatus.ACTIVE);

        assertThrows(ProjectNotFoundException.class,
                () -> projectService.updateProject(99L, request));
    }

    // ===== Delete Project =====
    @Test
    void deleteProject_WhenProjectExists_ShouldDelete() {
        Project project = new Project();
        project.setId(1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).delete(project);
    }

    // ===== Delete Project - Not Found =====
    @Test
    void deleteProject_WhenProjectNotFound_ShouldThrowException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> projectService.deleteProject(99L));
    }
}
