package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.MilestoneRequestDTO;
import com.ai_startuppilot.backend.dto.MilestoneResponseDTO;
import com.ai_startuppilot.backend.entity.Milestone;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.enums.MilestoneStatus;
import com.ai_startuppilot.backend.exception.MilestoneNotFoundException;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.mapper.MilestoneMapper;
import com.ai_startuppilot.backend.repository.MilestoneRepository;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MilestoneServiceTest {

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private MilestoneMapper milestoneMapper;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private MilestoneService milestoneService;

    private Project project;
    private Milestone milestone;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        milestone = new Milestone();
        milestone.setId(20L);
        milestone.setTitle("MVP Release");
        milestone.setStatus(MilestoneStatus.IN_PROGRESS);
        milestone.setProject(project);
    }

    // ===== Create Milestone =====
    @Test
    void createMilestone_WithValidProject_ShouldReturnMilestoneResponse() {
        MilestoneRequestDTO request = new MilestoneRequestDTO();
        request.setTitle("MVP Release");
        request.setStatus(MilestoneStatus.IN_PROGRESS);
        request.setProjectId(1L);

        MilestoneResponseDTO response = new MilestoneResponseDTO();
        response.setId(20L);
        response.setTitle("MVP Release");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(milestoneMapper.mapToEntity(request, project)).thenReturn(milestone);
        when(milestoneRepository.save(milestone)).thenReturn(milestone);
        when(milestoneMapper.mapToDto(milestone)).thenReturn(response);

        MilestoneResponseDTO result = milestoneService.createMilestone(request);

        assertEquals("MVP Release", result.getTitle());
        assertEquals(20L, result.getId());
        verify(milestoneRepository, times(1)).save(milestone);
    }

    // ===== Create Milestone - Invalid Project =====
    @Test
    void createMilestone_WithInvalidProject_ShouldThrowException() {
        MilestoneRequestDTO request = new MilestoneRequestDTO();
        request.setProjectId(999L);
        request.setTitle("X");
        request.setStatus(MilestoneStatus.NOT_STARTED);

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> milestoneService.createMilestone(request));
    }

    // ===== Get Milestone By ID - Found =====
    @Test
    void getMilestoneById_WhenExists_ShouldReturn() {
        MilestoneResponseDTO response = new MilestoneResponseDTO();
        response.setId(20L);
        response.setTitle("MVP Release");

        when(milestoneRepository.findById(20L)).thenReturn(Optional.of(milestone));
        when(milestoneMapper.mapToDto(milestone)).thenReturn(response);

        MilestoneResponseDTO result = milestoneService.getMilestoneById(20L);

        assertEquals(20L, result.getId());
        assertEquals("MVP Release", result.getTitle());
    }

    // ===== Get Milestone By ID - Not Found =====
    @Test
    void getMilestoneById_WhenNotFound_ShouldThrowException() {
        when(milestoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class,
                () -> milestoneService.getMilestoneById(99L));
    }

    // ===== Get All Milestones with Pagination =====
    @Test
    void getAllMilestones_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Milestone> page = new PageImpl<>(List.of(milestone), pageable, 1);

        MilestoneResponseDTO response = new MilestoneResponseDTO();
        response.setId(20L);

        when(milestoneRepository.findAll(pageable)).thenReturn(page);
        when(milestoneMapper.mapToDto(milestone)).thenReturn(response);

        Page<MilestoneResponseDTO> result = milestoneService.getAllMilestones(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(20L, result.getContent().get(0).getId());
    }

    // ===== Update Milestone - Not Found =====
    @Test
    void updateMilestone_WhenNotFound_ShouldThrowException() {
        MilestoneRequestDTO request = new MilestoneRequestDTO();
        request.setProjectId(1L);
        request.setTitle("Updated");
        request.setStatus(MilestoneStatus.COMPLETED);

        when(milestoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class,
                () -> milestoneService.updateMilestone(99L, request));
    }

    // ===== Delete Milestone =====
    @Test
    void deleteMilestone_WhenExists_ShouldDelete() {
        when(milestoneRepository.findById(20L)).thenReturn(Optional.of(milestone));

        milestoneService.deleteMilestone(20L);

        verify(milestoneRepository, times(1)).delete(milestone);
    }

    // ===== Delete Milestone - Not Found =====
    @Test
    void deleteMilestone_WhenNotFound_ShouldThrowException() {
        when(milestoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class,
                () -> milestoneService.deleteMilestone(99L));
    }
}
