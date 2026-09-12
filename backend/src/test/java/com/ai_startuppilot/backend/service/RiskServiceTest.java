package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.RiskRequestDTO;
import com.ai_startuppilot.backend.dto.RiskResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Risk;
import com.ai_startuppilot.backend.enums.RiskSeverity;
import com.ai_startuppilot.backend.enums.RiskStatus;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.exception.RiskNotFoundException;
import com.ai_startuppilot.backend.mapper.RiskMapper;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import com.ai_startuppilot.backend.repository.RiskRepository;
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

public class RiskServiceTest {

    @Mock
    private RiskRepository riskRepository;

    @Mock
    private RiskMapper riskMapper;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private RiskService riskService;

    private Project project;
    private Risk risk;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        risk = new Risk();
        risk.setId(30L);
        risk.setTitle("Data Breach");
        risk.setSeverity(RiskSeverity.CRITICAL);
        risk.setStatus(RiskStatus.OPEN);
        risk.setProject(project);
    }

    // ===== Create Risk =====
    @Test
    void createRisk_WithValidProject_ShouldReturn() {
        RiskRequestDTO request = new RiskRequestDTO();
        request.setTitle("Data Breach");
        request.setSeverity(RiskSeverity.CRITICAL);
        request.setStatus(RiskStatus.OPEN);
        request.setProjectId(1L);

        RiskResponseDTO response = new RiskResponseDTO();
        response.setId(30L);
        response.setTitle("Data Breach");
        response.setSeverity(RiskSeverity.CRITICAL);
        response.setStatus(RiskStatus.OPEN);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(riskMapper.mapToEntity(request, project)).thenReturn(risk);
        when(riskRepository.save(risk)).thenReturn(risk);
        when(riskMapper.mapToDto(risk)).thenReturn(response);

        RiskResponseDTO result = riskService.createRisk(request);

        assertEquals("Data Breach", result.getTitle());
        assertEquals(RiskSeverity.CRITICAL, result.getSeverity());
        assertEquals(RiskStatus.OPEN, result.getStatus());
        verify(riskRepository, times(1)).save(risk);
    }

    // ===== Create Risk - Invalid Project =====
    @Test
    void createRisk_WithInvalidProject_ShouldThrowException() {
        RiskRequestDTO request = new RiskRequestDTO();
        request.setProjectId(999L);
        request.setTitle("X");
        request.setSeverity(RiskSeverity.LOW);
        request.setStatus(RiskStatus.OPEN);

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> riskService.createRisk(request));
    }

    // ===== Get Risk by ID - Found =====
    @Test
    void getRiskById_WhenExists_ShouldReturn() {
        RiskResponseDTO response = new RiskResponseDTO();
        response.setId(30L);
        response.setSeverity(RiskSeverity.CRITICAL);
        response.setStatus(RiskStatus.OPEN);

        when(riskRepository.findById(30L)).thenReturn(Optional.of(risk));
        when(riskMapper.mapToDto(risk)).thenReturn(response);

        RiskResponseDTO result = riskService.getRiskById(30L);

        assertEquals(30L, result.getId());
        assertEquals(RiskSeverity.CRITICAL, result.getSeverity());
    }

    // ===== Get Risk by ID - Not Found =====
    @Test
    void getRiskById_WhenNotFound_ShouldThrowException() {
        when(riskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RiskNotFoundException.class,
                () -> riskService.getRiskById(99L));
    }

    // ===== Get All Risks with Pagination =====
    @Test
    void getAllRisks_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Risk> page = new PageImpl<>(List.of(risk), pageable, 1);

        RiskResponseDTO response = new RiskResponseDTO();
        response.setId(30L);

        when(riskRepository.findAll(pageable)).thenReturn(page);
        when(riskMapper.mapToDto(risk)).thenReturn(response);

        Page<RiskResponseDTO> result = riskService.getAllRisks(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(30L, result.getContent().get(0).getId());
    }

    // ===== Severity stays persisted =====
    @Test
    void createRisk_WithHighSeverity_ShouldPersistSeverity() {
        RiskRequestDTO request = new RiskRequestDTO();
        request.setTitle("Infra Failure");
        request.setSeverity(RiskSeverity.HIGH);
        request.setStatus(RiskStatus.IN_PROGRESS);
        request.setProjectId(1L);

        Risk highRisk = new Risk();
        highRisk.setId(31L);
        highRisk.setSeverity(RiskSeverity.HIGH);
        highRisk.setStatus(RiskStatus.IN_PROGRESS);
        highRisk.setProject(project);

        RiskResponseDTO response = new RiskResponseDTO();
        response.setId(31L);
        response.setSeverity(RiskSeverity.HIGH);
        response.setStatus(RiskStatus.IN_PROGRESS);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(riskMapper.mapToEntity(request, project)).thenReturn(highRisk);
        when(riskRepository.save(highRisk)).thenReturn(highRisk);
        when(riskMapper.mapToDto(highRisk)).thenReturn(response);

        RiskResponseDTO result = riskService.createRisk(request);

        assertEquals(RiskSeverity.HIGH, result.getSeverity());
        assertEquals(RiskStatus.IN_PROGRESS, result.getStatus());
    }

    // ===== Delete Risk =====
    @Test
    void deleteRisk_WhenExists_ShouldDelete() {
        when(riskRepository.findById(30L)).thenReturn(Optional.of(risk));

        riskService.deleteRisk(30L);

        verify(riskRepository, times(1)).delete(risk);
    }

    // ===== Delete Risk - Not Found =====
    @Test
    void deleteRisk_WhenNotFound_ShouldThrowException() {
        when(riskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RiskNotFoundException.class,
                () -> riskService.deleteRisk(99L));
    }
}
