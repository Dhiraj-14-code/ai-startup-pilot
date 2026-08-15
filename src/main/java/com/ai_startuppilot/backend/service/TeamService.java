package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.TeamRequestDTO;
import com.ai_startuppilot.backend.dto.TeamResponseDTO;
import com.ai_startuppilot.backend.dto.UserResponseDTO;
import com.ai_startuppilot.backend.entity.Team;
import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.exception.TeamNotFoundException;
import com.ai_startuppilot.backend.exception.UserAlreadyMemberException;
import com.ai_startuppilot.backend.exception.UserNotFoundException;
import com.ai_startuppilot.backend.exception.UserNotMemberException;
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

    public TeamService(
            TeamRepository teamRepository,
            UserRepository userRepository,
            TeamMapper teamMapper,
            UserMapper userMapper) {

        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.teamMapper = teamMapper;
        this.userMapper = userMapper;
    }

    // Create Team
    public TeamResponseDTO createTeam(
            TeamRequestDTO requestDTO) {

        // DTO → Entity
        Team team = teamMapper.mapToEntity(requestDTO);

        // Database mein save
        Team savedTeam = teamRepository.save(team);

        // Entity → Response DTO
        return teamMapper.mapToDto(savedTeam);
    }

    // Get All Teams
    public List<TeamResponseDTO> getAllTeams() {

        List<Team> teams = teamRepository.findAll();

        return teams.stream()
                .map(teamMapper::mapToDto)
                .toList();
    }

    // Get Team By ID
    public TeamResponseDTO getTeamById(Long id) {

        Team team = teamRepository.findById(id)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team ID " + id + " not found"));

        return teamMapper.mapToDto(team);
    }

    // Update Team
    public TeamResponseDTO updateTeam(
            Long id,
            TeamRequestDTO requestDTO) {

        // Existing team find karo
        Team team = teamRepository.findById(id)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team ID " + id + " not found"));

        // Existing fields update karo
        team.setName(requestDTO.getName());
        team.setDescription(requestDTO.getDescription());

        // Updated team save karo
        Team updatedTeam = teamRepository.save(team);

        return teamMapper.mapToDto(updatedTeam);
    }

    // Delete Team
    public void deleteTeam(Long id) {

        Team team = teamRepository.findById(id)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team ID " + id + " not found"));

        teamRepository.delete(team);
    }

    // Add Member
    public void addMember(Long teamId, Long userId) {

        // Team exist karta hai?
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team ID " + teamId + " not found"));

        // User exist karta hai?
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User ID " + userId + " not found"));

        // Check karo user already member toh nahi hai
        if (team.getMembers().contains(user)) {

            throw new UserAlreadyMemberException(
                    "User ID " + userId
                            + " is already a member of Team ID "
                            + teamId);
        }

        // User ko team mein add karo
        team.getMembers().add(user);

        teamRepository.save(team);
    }

    // Get All Members
    public List<UserResponseDTO> getTeamMembers(
            Long teamId) {

        // Team exist karta hai?
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team ID " + teamId + " not found"));

        return team.getMembers()
                .stream()
                .map(userMapper::mapToDto)
                .toList();
    }

    // Remove Team Member
    public void removeMember(
            Long teamId,
            Long userId) {

        // Team exist karta hai?
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team ID " + teamId + " not found"));

        // User exist karta hai?
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User ID " + userId + " not found"));

        // Check karo user team ka member hai ya nahi
        if (!team.getMembers().contains(user)) {

            throw new UserNotMemberException(
                    "User ID " + userId
                            + " is not a member of Team ID "
                            + teamId);
        }

        // Team se user remove karo
        team.getMembers().remove(user);

        teamRepository.save(team);
    }
}