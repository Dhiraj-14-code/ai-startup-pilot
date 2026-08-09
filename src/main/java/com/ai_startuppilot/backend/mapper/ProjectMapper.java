package com.ai_startuppilot.backend.mapper;

import com.ai_startuppilot.backend.dto.ProjectRequestDTO;
import com.ai_startuppilot.backend.dto.ProjectResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    //map dto to entity
    public Project mapToEntity(ProjectRequestDTO dto){
        Project project = new Project();

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStatus(dto.getStatus());

        return project;
    }
    //map entity to dto
    public ProjectResponseDTO mapToDto(Project project){
        ProjectResponseDTO dto = new ProjectResponseDTO();

        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        return dto;
    }
}
