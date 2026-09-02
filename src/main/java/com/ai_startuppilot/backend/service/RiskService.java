package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.RiskRequestDTO;
import com.ai_startuppilot.backend.dto.RiskResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Risk;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.exception.RiskNotFoundException;
import com.ai_startuppilot.backend.mapper.RiskMapper;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import com.ai_startuppilot.backend.repository.RiskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskService {

    private final RiskRepository riskRepository;
    private final RiskMapper riskMapper;
    private final ProjectRepository projectRepository;

    public RiskService(
            RiskRepository riskRepository,
            RiskMapper riskMapper,
            ProjectRepository projectRepository) {

        this.riskRepository = riskRepository;
        this.riskMapper = riskMapper;
        this.projectRepository = projectRepository;
    }

    // Create Risk
    public RiskResponseDTO createRisk(RiskRequestDTO requestDTO) {

        Project project = projectRepository.findById(
                requestDTO.getProjectId()
        ).orElseThrow(() ->
                new ProjectNotFoundException(
                        "Project ID " + requestDTO.getProjectId() + " not found"));

        Risk risk = riskMapper.mapToEntity(
                requestDTO,
                project
        );

        Risk savedRisk = riskRepository.save(risk);

        return riskMapper.mapToDto(savedRisk);
    }

    // Get all Risks
    public Page<RiskResponseDTO> getAllRisks(Pageable pageable) {

        Page<Risk> risks = riskRepository.findAll(pageable);

        return risks.map(riskMapper::mapToDto);
    }

    // Get Risk by ID
    public RiskResponseDTO getRiskById(Long id) {

        Risk risk = riskRepository.findById(id)
                .orElseThrow(() ->
                        new RiskNotFoundException(
                                "Risk ID " + id + " not found"));

        return riskMapper.mapToDto(risk);
    }

    // Update Risk
    public RiskResponseDTO updateRisk(
            Long id,
            RiskRequestDTO requestDTO) {

        Risk risk = riskRepository.findById(id)
                .orElseThrow(() ->
                        new RiskNotFoundException(
                                "Risk ID " + id + " not found"));

        Project project = projectRepository.findById(
                requestDTO.getProjectId()
        ).orElseThrow(() ->
                new ProjectNotFoundException(
                        "Project ID " + requestDTO.getProjectId() + " not found"));

        risk.setTitle(requestDTO.getTitle());
        risk.setDescription(requestDTO.getDescription());
        risk.setSeverity(requestDTO.getSeverity());
        risk.setStatus(requestDTO.getStatus());
        risk.setProject(project);

        Risk updatedRisk = riskRepository.save(risk);

        return riskMapper.mapToDto(updatedRisk);
    }

    // Delete Risk
    public void deleteRisk(Long id) {

        Risk risk = riskRepository.findById(id)
                .orElseThrow(() ->
                        new RiskNotFoundException(
                                "Risk ID " + id + " not found"));

        riskRepository.delete(risk);
    }
}