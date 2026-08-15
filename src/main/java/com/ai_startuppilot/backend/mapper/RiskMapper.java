package com.ai_startuppilot.backend.mapper;

import com.ai_startuppilot.backend.dto.RiskRequestDTO;
import com.ai_startuppilot.backend.dto.RiskResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Risk;
import org.springframework.stereotype.Component;

@Component
public class RiskMapper {

    // DTO → Entity
    public Risk mapToEntity(
            RiskRequestDTO requestDTO,
            Project project) {

        Risk risk = new Risk();

        risk.setTitle(requestDTO.getTitle());
        risk.setDescription(requestDTO.getDescription());
        risk.setSeverity(requestDTO.getSeverity());
        risk.setStatus(requestDTO.getStatus());

        // Project ID nahi, actual Project entity set karenge
        risk.setProject(project);

        return risk;
    }

    // Entity → Response DTO
    public RiskResponseDTO mapToDto(Risk risk) {

        RiskResponseDTO responseDTO = new RiskResponseDTO();

        responseDTO.setId(risk.getId());
        responseDTO.setTitle(risk.getTitle());
        responseDTO.setDescription(risk.getDescription());
        responseDTO.setSeverity(risk.getSeverity());
        responseDTO.setStatus(risk.getStatus());

        // Response mein sirf Project ID
        responseDTO.setProjectId(
                risk.getProject().getId()
        );

        responseDTO.setCreatedAt(risk.getCreatedAt());
        responseDTO.setUpdatedAt(risk.getUpdatedAt());

        return responseDTO;
    }
}