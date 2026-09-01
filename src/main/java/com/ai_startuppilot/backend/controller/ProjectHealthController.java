package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.ProjectHealthResponseDTO;
import com.ai_startuppilot.backend.service.ProjectHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectHealthController {

    private final ProjectHealthService projectHealthService;

    // Get project health analysis
    @GetMapping("/{projectId}/health")
    public ResponseEntity<ProjectHealthResponseDTO> getProjectHealth(
            @PathVariable Long projectId) {

        ProjectHealthResponseDTO responseDTO =
                projectHealthService.getProjectHealth(projectId);

        return ResponseEntity.ok(responseDTO);
    }
}