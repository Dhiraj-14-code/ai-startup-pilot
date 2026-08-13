package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.ProjectRequestDTO;
import com.ai_startuppilot.backend.dto.ProjectResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.mapper.ProjectMapper;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.cfg.MapperBuilder;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final MapperBuilder mapperBuilder;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, MapperBuilder mapperBuilder) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.mapperBuilder = mapperBuilder;
    }
    //Create Project
    public ProjectResponseDTO createProject(ProjectRequestDTO requestDTO){
        // 1. Convert dto to entity
        Project project = projectMapper.mapToEntity(requestDTO);

        // Saved entity to database
        Project savedProject=projectRepository.save(project);

        //Convert entity to dto
        return projectMapper.mapToDto(savedProject);

    }
    //Get All Projects
    public List<ProjectResponseDTO> getAllProjects(){
        List<Project> project = projectRepository.findAll();
        return project.stream()
                .map(projectMapper::mapToDto)
                .toList();
    }
    //Get project by id
    public ProjectResponseDTO getProjectByID(Long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(()->new ProjectNotFoundException("Project Not Found!!"));
        return projectMapper.mapToDto(project);
    }
    //Update project by id
    public ProjectResponseDTO updateProject(
            Long id ,
            ProjectRequestDTO requestDTO
    ){
        Project project = projectRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Project Id is not founnd!"));

        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());
        project.setStatus(requestDTO.getStatus());

        Project updatedProject = projectRepository.save(project);

        return projectMapper.mapToDto(updatedProject);
    }
    //Delete Project
    public void deleteProject(Long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(()->new ProjectNotFoundException("Project Not Found!!"));
        projectRepository.delete(project);
    }

}
