package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.TeamRequestDTO;
import com.ai_startuppilot.backend.dto.TeamResponseDTO;
import com.ai_startuppilot.backend.dto.UserResponseDTO;
import com.ai_startuppilot.backend.entity.Team;
import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.mapper.TeamMapper;
import com.ai_startuppilot.backend.mapper.UserMapper;
import com.ai_startuppilot.backend.repository.TeamRepository;
import com.ai_startuppilot.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
    private final UserMapper userMapper;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository, TeamMapper teamMapper, UserMapper userMapper) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.teamMapper = teamMapper;
        this.userMapper = userMapper;
    }

    //Create Team
    public TeamResponseDTO createTeam(TeamRequestDTO requestDTO){
        Team team = teamMapper.mapToEntity(requestDTO);
        Team savedTeam = teamRepository.save(team);
        return teamMapper.mapToDto(savedTeam);
    }
    //Get All Teams
    public List<TeamResponseDTO> getAllTeams(){
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(teamMapper::mapToDto)
                .toList();
    }
    //Get Team By id
    public TeamResponseDTO getTeamById(Long id ){
        Team team = teamRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Team is not found!!"));
        return teamMapper.mapToDto(team);
    }
    //Update Team by id
    public TeamResponseDTO updateTeam(Long id ,TeamRequestDTO requestDTO){
        Team team = teamRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Team not found!!"));

        team.setName(requestDTO.getName());
        team.setDescription(requestDTO.getDescription());

        Team updatedTeam = teamRepository.save(team);

        return teamMapper.mapToDto(updatedTeam);
    }
    //Delete Team
    public void deleteTeam(Long id){
        Team team = teamRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Team not found!!"));
    teamRepository.delete(team);
    }
    //add member
    public void addMember(Long teamId,Long userId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()->new RuntimeException("Team not found!"));
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found!!"));

        if (team.getMembers().contains(user)){
            throw new RuntimeException("User is already exists in Team");
        }
        team.getMembers().add(user);
        teamRepository.save(team);
    }
    //Get  all members
    public List<UserResponseDTO> getTeamMembers(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()->new RuntimeException("Team not found!"));
        return team.getMembers()
                .stream()
                .map(userMapper::mapToDto)
                .toList();
    }
    //Remove team member
    public void removeMember(Long teamId, Long userId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException("Team not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!team.getMembers().contains(user)) {
            throw new RuntimeException("User is not a member of this team");
        }

        team.getMembers().remove(user);

        teamRepository.save(team);
    }
}
