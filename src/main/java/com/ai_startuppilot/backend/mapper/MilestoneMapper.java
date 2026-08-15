package com.ai_startuppilot.backend.mapper;

import com.ai_startuppilot.backend.dto.MilestoneRequestDTO;
import com.ai_startuppilot.backend.dto.MilestoneResponseDTO;
import com.ai_startuppilot.backend.entity.Milestone;
import com.ai_startuppilot.backend.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    // DTO → Entity
    // Project entity service se already find hokar aayegi
    public Milestone mapToEntity(
            MilestoneRequestDTO dto,
            Project project) {

        Milestone milestone = new Milestone();

        milestone.setTitle(dto.getTitle());
        milestone.setDescription(dto.getDescription());
        milestone.setStatus(dto.getStatus());
        milestone.setDueDate(dto.getDueDate());

        // Project ID nahi, actual Project entity set kar rahe hain
        milestone.setProject(project);

        return milestone;
    }

    // Entity → Response DTO
    public MilestoneResponseDTO mapToDto(Milestone milestone) {

        MilestoneResponseDTO dto = new MilestoneResponseDTO();

        dto.setId(milestone.getId());
        dto.setTitle(milestone.getTitle());
        dto.setDescription(milestone.getDescription());
        dto.setStatus(milestone.getStatus());
        dto.setDueDate(milestone.getDueDate());

        // Response mein sirf project ID bhejenge
        dto.setProjectId(milestone.getProject().getId());

        dto.setCreatedAt(milestone.getCreatedAt());
        dto.setUpdatedAt(milestone.getUpdatedAt());

        return dto;
    }
}