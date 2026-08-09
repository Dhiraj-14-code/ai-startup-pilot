package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.ProjectRequestDTO;
import com.ai_startuppilot.backend.dto.ProjectResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
        public final ProjectService projectService;

        public ProjectController(ProjectService projectService){
            this.projectService=projectService;
        }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO requestDTO) {

        ProjectResponseDTO responseDTO =
                projectService.createProject(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDTO);
    }
    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects(){
            List<ProjectResponseDTO> responseDTO=projectService.getAllProjects();
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(responseDTO);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectByID(
           @PathVariable Long id
    ){
            ProjectResponseDTO responseDTO = projectService.getProjectByID(id);
            return ResponseEntity.ok(responseDTO);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long id ,
            @RequestBody ProjectRequestDTO requestDTO
    ){
            ProjectResponseDTO responseDTO = projectService.updateProject(id,requestDTO);
            return ResponseEntity.ok(responseDTO);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(
            @PathVariable Long id
    ){
         projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
