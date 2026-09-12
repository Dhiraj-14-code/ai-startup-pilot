package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.ProjectRequestDTO;
import com.ai_startuppilot.backend.dto.ProjectResponseDTO;
import com.ai_startuppilot.backend.enums.ProjectStatus;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests using standalone MockMvc (no Spring context needed).
 * Security is intentionally bypassed here — see SecurityIntegrationTest for security-specific tests.
 */
public class ProjectControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Standalone setup — no Spring Security, tests controller logic only
        mockMvc = MockMvcBuilders
                .standaloneSetup(projectController)
                // Register the GlobalExceptionHandler so 404/400 are returned correctly
                .setControllerAdvice(new com.ai_startuppilot.backend.exception.GlobalExceptionHandler())
                .build();
    }

    // ===== POST /api/v1/projects - Valid =====
    @Test
    void createProject_WithValidRequest_ShouldReturn201() throws Exception {
        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("Alpha Startup");
        request.setStatus(ProjectStatus.ACTIVE);

        ProjectResponseDTO response = new ProjectResponseDTO();
        response.setId(1L);
        response.setName("Alpha Startup");
        response.setStatus(ProjectStatus.ACTIVE);

        when(projectService.createProject(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alpha Startup"));
    }

    // ===== POST /api/v1/projects - Blank name (validation 400) =====
    @Test
    void createProject_WithBlankName_ShouldReturn400() throws Exception {
        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("");
        request.setStatus(ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/v1/projects - Name too short (< 3 chars) =====
    @Test
    void createProject_WithTooShortName_ShouldReturn400() throws Exception {
        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("AB");
        request.setStatus(ProjectStatus.ACTIVE);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/v1/projects - Missing status (validation 400) =====
    @Test
    void createProject_WithMissingStatus_ShouldReturn400() throws Exception {
        String requestJson = "{\"name\":\"Valid Project Name\"}";

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /api/v1/projects =====
    @Test
    void getAllProjects_ShouldReturn200WithList() throws Exception {
        ProjectResponseDTO p = new ProjectResponseDTO();
        p.setId(1L);
        p.setName("Alpha");

        when(projectService.getAllProjects()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alpha"));
    }

    // ===== GET /api/v1/projects/{id} - Found =====
    @Test
    void getProjectById_WhenExists_ShouldReturn200() throws Exception {
        ProjectResponseDTO p = new ProjectResponseDTO();
        p.setId(1L);
        p.setName("Alpha");

        when(projectService.getProjectByID(1L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alpha"));
    }

    // ===== GET /api/v1/projects/{id} - Not Found (404) =====
    @Test
    void getProjectById_WhenNotFound_ShouldReturn404() throws Exception {
        when(projectService.getProjectByID(99L))
                .thenThrow(new ProjectNotFoundException("Project ID 99 not found"));

        mockMvc.perform(get("/api/v1/projects/99"))
                .andExpect(status().isNotFound());
    }

    // ===== PUT /api/v1/projects/{id} - Update =====
    @Test
    void updateProject_WhenExists_ShouldReturn200() throws Exception {
        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("Updated Alpha");
        request.setStatus(ProjectStatus.COMPLETED);

        ProjectResponseDTO response = new ProjectResponseDTO();
        response.setId(1L);
        response.setName("Updated Alpha");

        when(projectService.updateProject(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Alpha"));
    }

    // ===== PUT /api/v1/projects/{id} - Not Found =====
    @Test
    void updateProject_WhenNotFound_ShouldReturn404() throws Exception {
        when(projectService.updateProject(eq(99L), any()))
                .thenThrow(new ProjectNotFoundException("Project ID 99 not found"));

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("Updated Name");
        request.setStatus(ProjectStatus.ACTIVE);

        mockMvc.perform(put("/api/v1/projects/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ===== DELETE /api/v1/projects/{id} =====
    @Test
    void deleteProject_WhenExists_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isNoContent());
    }

    // ===== DELETE /api/v1/projects/{id} - Not Found =====
    @Test
    void deleteProject_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ProjectNotFoundException("Project ID 99 not found"))
                .when(projectService).deleteProject(99L);

        mockMvc.perform(delete("/api/v1/projects/99"))
                .andExpect(status().isNotFound());
    }
}
