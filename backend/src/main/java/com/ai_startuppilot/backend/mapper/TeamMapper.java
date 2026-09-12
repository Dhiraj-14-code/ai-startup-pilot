package com.ai_startuppilot.backend.mapper;

import com.ai_startuppilot.backend.dto.TeamRequestDTO;
import com.ai_startuppilot.backend.dto.TeamResponseDTO;
import com.ai_startuppilot.backend.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    //Dto to entity
    public Team mapToEntity(TeamRequestDTO requestDTO){
        Team team = new Team();

        team.setName(requestDTO.getName());
        team.setDescription(requestDTO.getDescription());

        return team;
    }
    //Entity to Dto
    public TeamResponseDTO mapToDto(Team team){
        TeamResponseDTO responseDTO = new TeamResponseDTO();

        responseDTO.setId(team.getId());
        responseDTO.setName(team.getName());
        responseDTO.setDescription(team.getDescription());
        responseDTO.setCreatedAt(team.getCreatedAt());
        responseDTO.setUpdatedAt(team.getUpdatedAt());

        return responseDTO;
    }

}
