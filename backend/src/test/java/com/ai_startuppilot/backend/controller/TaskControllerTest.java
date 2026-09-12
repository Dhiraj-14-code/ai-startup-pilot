package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.TaskRequestDTO;
import com.ai_startuppilot.backend.dto.TaskResponseDTO;
import com.ai_startuppilot.backend.enums.TaskPriority;
import com.ai_startuppilot.backend.enums.TaskStatus;
import com.ai_startuppilot.backend.exception.TaskNotFoundException;
import com.ai_startuppilot.backend.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests using standalone MockMvc (no Spring context needed).
 * Security is intentionally bypassed — see SecurityIntegrationTest for security-specific tests.
 */
public class TaskControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
                .setControllerAdvice(new com.ai_startuppilot.backend.exception.GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // ===== POST /api/v1/tasks - Valid =====
    @Test
    void createTask_WithValidRequest_ShouldReturn201() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Fix Critical Bug");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);
        request.setProjectId(1L);

        TaskResponseDTO response = new TaskResponseDTO();
        response.setId(10L);
        response.setTitle("Fix Critical Bug");

        when(taskService.createTask(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Fix Critical Bug"));
    }

    // ===== POST /api/v1/tasks - Blank title (validation 400) =====
    @Test
    void createTask_WithBlankTitle_ShouldReturn400() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);
        request.setProjectId(1L);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/v1/tasks - Missing project ID (validation 400) =====
    @Test
    void createTask_WithMissingProjectId_ShouldReturn400() throws Exception {
        // projectId is null → @NotNull fails
        String requestJson = "{\"title\":\"Valid Title\",\"status\":\"TODO\",\"priority\":\"HIGH\"}";

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /api/v1/tasks - Pagination =====
    @Test
    void getAllTasks_ShouldReturnPagedResponse() throws Exception {
        TaskResponseDTO t = new TaskResponseDTO();
        t.setId(10L);
        t.setTitle("Fix Bug");

        Page<TaskResponseDTO> page = new PageImpl<>(List.of(t), PageRequest.of(0, 5), 1);
        when(taskService.getAllTask(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Fix Bug"));
    }

    // ===== GET /api/v1/tasks - Empty page =====
    @Test
    void getAllTasks_WhenEmpty_ShouldReturnEmptyPage() throws Exception {
        Page<TaskResponseDTO> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);
        when(taskService.getAllTask(any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/tasks?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ===== GET /api/v1/tasks/{id} - Found =====
    @Test
    void getTaskById_WhenExists_ShouldReturn200() throws Exception {
        TaskResponseDTO response = new TaskResponseDTO();
        response.setId(10L);
        response.setTitle("Fix Bug");

        when(taskService.getTaskById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tasks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Fix Bug"));
    }

    // ===== GET /api/v1/tasks/{id} - Not Found (404) =====
    @Test
    void getTaskById_WhenNotFound_ShouldReturn404() throws Exception {
        when(taskService.getTaskById(99L))
                .thenThrow(new TaskNotFoundException("Task ID 99 not found"));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound());
    }

    // ===== DELETE /api/v1/tasks/{id} - Not Found =====
    @Test
    void deleteTask_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new TaskNotFoundException("Task ID 99 not found"))
                .when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/api/v1/tasks/99"))
                .andExpect(status().isNotFound());
    }
}
