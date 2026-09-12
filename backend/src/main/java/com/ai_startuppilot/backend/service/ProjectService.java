package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.ProjectRequestDTO;
import com.ai_startuppilot.backend.dto.ProjectResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.mapper.ProjectMapper;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper) {

        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    // Create Project
    public ProjectResponseDTO createProject(
            ProjectRequestDTO requestDTO) {

        // DTO → Entity
        Project project =
                projectMapper.mapToEntity(requestDTO);

        // Entity database mein save
        Project savedProject =
                projectRepository.save(project);

        // Entity → Response DTO
        return projectMapper.mapToDto(savedProject);
    }

    // Get All Projects
    public List<ProjectResponseDTO> getAllProjects() {

        List<Project> projects =
                projectRepository.findAll();

        return projects.stream()
                .map(projectMapper::mapToDto)
                .toList();
    }

    // Get Project by ID
    public ProjectResponseDTO getProjectByID(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project ID " + id + " not found"));

        return projectMapper.mapToDto(project);
    }

    // Update Project
    public ProjectResponseDTO updateProject(
            Long id,
            ProjectRequestDTO requestDTO) {

        // Existing project find karo
        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project ID " + id + " not found"));

        // Existing project ke fields update karo
        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());
        project.setStatus(requestDTO.getStatus());

        // Updated project save karo
        Project updatedProject =
                projectRepository.save(project);

        // Entity → Response DTO
        return projectMapper.mapToDto(updatedProject);
    }

    // Delete Project
    public void deleteProject(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project ID " + id + " not found"));

        projectRepository.delete(project);
    }
}