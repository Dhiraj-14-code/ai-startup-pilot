package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.TaskRequestDTO;
import com.ai_startuppilot.backend.dto.TaskResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Task;
import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.enums.TaskPriority;
import com.ai_startuppilot.backend.enums.TaskStatus;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.exception.TaskNotFoundException;
import com.ai_startuppilot.backend.exception.UserNotFoundException;
import com.ai_startuppilot.backend.mapper.TaskMapper;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import com.ai_startuppilot.backend.repository.TaskRepository;
import com.ai_startuppilot.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Project project;
    private Task task;
    private TaskRequestDTO request;
    private TaskResponseDTO response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        task = new Task();
        task.setId(10L);
        task.setTitle("Implement Feature X");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setProject(project);

        request = new TaskRequestDTO();
        request.setTitle("Implement Feature X");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);
        request.setProjectId(1L);

        response = new TaskResponseDTO();
        response.setId(10L);
        response.setTitle("Implement Feature X");
        response.setProjectId(1L);
    }

    // ===== Create Task =====
    @Test
    void createTask_WithValidProjectAndNoUser_ShouldReturnTaskResponse() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskMapper.mapToEntity(request, project, null)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.mapToDto(task)).thenReturn(response);

        TaskResponseDTO result = taskService.createTask(request);

        assertEquals("Implement Feature X", result.getTitle());
        assertEquals(1L, result.getProjectId());
        verify(taskRepository, times(1)).save(task);
    }

    // ===== Create Task with assigned user =====
    @Test
    void createTask_WithValidAssignedUser_ShouldSetUserOnTask() {
        User user = new User();
        user.setId(5L);
        request.setAssignedUserId(5L);

        Task taskWithUser = new Task();
        taskWithUser.setId(10L);
        taskWithUser.setTitle("Implement Feature X");
        taskWithUser.setProject(project);
        taskWithUser.setAssignedUser(user);

        TaskResponseDTO responseWithUser = new TaskResponseDTO();
        responseWithUser.setId(10L);
        responseWithUser.setAssignedUserId(5L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(taskMapper.mapToEntity(request, project, user)).thenReturn(taskWithUser);
        when(taskRepository.save(taskWithUser)).thenReturn(taskWithUser);
        when(taskMapper.mapToDto(taskWithUser)).thenReturn(responseWithUser);

        TaskResponseDTO result = taskService.createTask(request);

        assertEquals(5L, result.getAssignedUserId());
    }

    // ===== Create Task - Invalid Project =====
    @Test
    void createTask_WithInvalidProject_ShouldThrowProjectNotFoundException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> taskService.createTask(request),
                "Should throw ProjectNotFoundException for invalid projectId");
    }

    // ===== Create Task - Invalid Assigned User =====
    @Test
    void createTask_WithInvalidAssignedUser_ShouldThrowUserNotFoundException() {
        request.setAssignedUserId(999L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> taskService.createTask(request),
                "Should throw UserNotFoundException for invalid assignedUserId");
    }

    // ===== Get Task by ID - Found =====
    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskMapper.mapToDto(task)).thenReturn(response);

        TaskResponseDTO result = taskService.getTaskById(10L);

        assertEquals(10L, result.getId());
        assertEquals("Implement Feature X", result.getTitle());
    }

    // ===== Get Task by ID - Not Found =====
    @Test
    void getTaskById_WhenTaskNotFound_ShouldThrowException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.getTaskById(99L));
    }

    // ===== Get All Tasks with Pagination =====
    @Test
    void getAllTask_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Task> taskPage = new PageImpl<>(List.of(task), pageable, 1);

        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        when(taskMapper.mapToDto(task)).thenReturn(response);

        Page<TaskResponseDTO> result = taskService.getAllTask(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Implement Feature X", result.getContent().get(0).getTitle());
    }

    // ===== Pagination - Empty Page =====
    @Test
    void getAllTask_WhenNoTasks_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Task> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(taskRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<TaskResponseDTO> result = taskService.getAllTask(pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ===== Update Task =====
    @Test
    void updateTask_WhenTaskExists_ShouldUpdateAndReturn() {
        TaskRequestDTO updateRequest = new TaskRequestDTO();
        updateRequest.setTitle("Updated Title");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.MEDIUM);
        updateRequest.setProjectId(1L);

        Task updatedTask = new Task();
        updatedTask.setId(10L);
        updatedTask.setTitle("Updated Title");
        updatedTask.setProject(project);

        TaskResponseDTO updatedResponse = new TaskResponseDTO();
        updatedResponse.setId(10L);
        updatedResponse.setTitle("Updated Title");

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.save(task)).thenReturn(updatedTask);
        when(taskMapper.mapToDto(updatedTask)).thenReturn(updatedResponse);

        TaskResponseDTO result = taskService.updateTask(10L, updateRequest);

        assertEquals("Updated Title", result.getTitle());
        verify(taskRepository, times(1)).save(task);
    }

    // ===== Update Task - Not Found =====
    @Test
    void updateTask_WhenTaskNotFound_ShouldThrowException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(99L, request));
    }

    // ===== Delete Task =====
    @Test
    void deleteTask_WhenTaskExists_ShouldDelete() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        taskService.deleteTask(10L);

        verify(taskRepository, times(1)).delete(task);
    }

    // ===== Delete Task - Not Found =====
    @Test
    void deleteTask_WhenTaskNotFound_ShouldThrowException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class,
                () -> taskService.deleteTask(99L));
    }
}
