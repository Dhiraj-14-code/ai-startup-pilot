package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.TeamRequestDTO;
import com.ai_startuppilot.backend.dto.TeamResponseDTO;
import com.ai_startuppilot.backend.dto.UserResponseDTO;
import com.ai_startuppilot.backend.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }
    @PostMapping
    public ResponseEntity<TeamResponseDTO> createTeam(
            @Valid
            @RequestBody TeamRequestDTO requestDTO
            ){
        TeamResponseDTO responseDTO = teamService.createTeam(requestDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }
    @GetMapping
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams(){
        List<TeamResponseDTO> responseDTO= teamService.getAllTeams();
        return ResponseEntity.ok(responseDTO);
    }
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> getTeamById(
            @PathVariable Long id
    ){
        TeamResponseDTO responseDTO = teamService.getTeamById(id);
        return ResponseEntity.ok(responseDTO);
    }
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> updateTeam(
            @PathVariable Long id ,
            @RequestBody TeamRequestDTO requestDTO
    ){
        TeamResponseDTO responseDTO = teamService.updateTeam(id,requestDTO);
        return ResponseEntity.ok(responseDTO);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long id){
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> addMembers(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ){
        teamService.addMember(teamId,userId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<UserResponseDTO>> getTeamMembers(
            @PathVariable Long teamId
    ){
        List<UserResponseDTO> responseDTO=teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(responseDTO);
    }
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long userId) {

        teamService.removeMember(teamId, userId);

        return ResponseEntity.noContent().build();
    }}
