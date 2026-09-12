package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.MilestoneRequestDTO;
import com.ai_startuppilot.backend.dto.MilestoneResponseDTO;
import com.ai_startuppilot.backend.entity.Milestone;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.exception.MilestoneNotFoundException;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.mapper.MilestoneMapper;
import com.ai_startuppilot.backend.repository.MilestoneRepository;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneMapper milestoneMapper;
    private final ProjectRepository projectRepository;

    public MilestoneService(
            MilestoneRepository milestoneRepository,
            MilestoneMapper milestoneMapper,
            ProjectRepository projectRepository) {

        this.milestoneRepository = milestoneRepository;
        this.milestoneMapper = milestoneMapper;
        this.projectRepository = projectRepository;
    }

    // Create Milestone
    public MilestoneResponseDTO createMilestone(
            MilestoneRequestDTO requestDTO) {

        Project project = projectRepository.findById(
                requestDTO.getProjectId()
        ).orElseThrow(() -> new ProjectNotFoundException("Project not found!!"));
        Milestone milestone =
                milestoneMapper.mapToEntity(requestDTO, project);

        Milestone savedMilestone =
                milestoneRepository.save(milestone);

        return milestoneMapper.mapToDto(savedMilestone);
    }

    // Get all milestones
    public Page<MilestoneResponseDTO> getAllMilestones(Pageable pageable) {

        Page<Milestone> milestones =
                milestoneRepository.findAll(pageable);

        return milestones.map(milestoneMapper::mapToDto);
    }

    // Get milestone by ID
    public MilestoneResponseDTO getMilestoneById(Long id) {

        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() ->
                        new MilestoneNotFoundException(
                                "Milestone ID " + id + " not found"));
        return milestoneMapper.mapToDto(milestone);
    }

    // Update milestone
    public MilestoneResponseDTO updateMilestone(
            Long id,
            MilestoneRequestDTO requestDTO) {

        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() ->
                        new MilestoneNotFoundException(
                                "Milestone ID " + id + " not found"));
        Project project = projectRepository.findById(
                requestDTO.getProjectId()
        ).orElseThrow(() ->new ProjectNotFoundException("Project not found!!"));
        milestone.setTitle(requestDTO.getTitle());
        milestone.setDescription(requestDTO.getDescription());
        milestone.setStatus(requestDTO.getStatus());
        milestone.setDueDate(requestDTO.getDueDate());
        milestone.setProject(project);

        Milestone updatedMilestone =
                milestoneRepository.save(milestone);

        return milestoneMapper.mapToDto(updatedMilestone);
    }

    // Delete milestone
    public void deleteMilestone(Long id) {

        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() ->
                        new MilestoneNotFoundException(
                                "Milestone ID " + id + " not found"));
        milestoneRepository.delete(milestone);
    }
}