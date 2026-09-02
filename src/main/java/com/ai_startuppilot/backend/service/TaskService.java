package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.TaskRequestDTO;
import com.ai_startuppilot.backend.dto.TaskResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Task;
import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.exception.ProjectNotFoundException;
import com.ai_startuppilot.backend.exception.TaskNotFoundException;
import com.ai_startuppilot.backend.exception.UserNotFoundException;
import com.ai_startuppilot.backend.mapper.TaskMapper;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import com.ai_startuppilot.backend.repository.TaskRepository;
import com.ai_startuppilot.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // Create Task
    public TaskResponseDTO createTask(TaskRequestDTO requestDTO) {

        // Pehle check karo ki Project exist karta hai ya nahi
        Project project = projectRepository.findById(
                requestDTO.getProjectId()
        ).orElseThrow(() ->
                new ProjectNotFoundException(
                        "Project ID " + requestDTO.getProjectId()
                                + " not found"));

        User assignedUser = null;

        // Agar assignedUserId diya gaya hai,
        // toh User database mein exist karta hai ya nahi check karo
        if (requestDTO.getAssignedUserId() != null) {

            assignedUser = userRepository.findById(
                    requestDTO.getAssignedUserId()
            ).orElseThrow(() ->
                    new UserNotFoundException(
                            "User ID " + requestDTO.getAssignedUserId()
                                    + " not found"));
        }

        // DTO ko Entity mein convert kar rahe hain
        Task task = taskMapper.mapToEntity(
                requestDTO,
                project,
                assignedUser
        );

        // Database mein Task save kar rahe hain
        Task savedTask = taskRepository.save(task);

        // Entity ko Response DTO mein convert karke return
        return taskMapper.mapToDto(savedTask);
    }

    // Get all Tasks
    public Page<TaskResponseDTO> getAllTask(Pageable pageable) {

        Page<Task> tasks = taskRepository.findAll(pageable);

        return tasks.map(taskMapper::mapToDto);
    }

    // Get Task by ID
    public TaskResponseDTO getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task ID " + id + " not found"));

        return taskMapper.mapToDto(task);
    }

    // Update Task
    public TaskResponseDTO updateTask(
            Long id,
            TaskRequestDTO requestDTO) {

        // Pehle existing Task find karo
        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task ID " + id + " not found"));

        // Updated Project exist karta hai ya nahi
        Project project = projectRepository.findById(
                requestDTO.getProjectId()
        ).orElseThrow(() ->
                new ProjectNotFoundException(
                        "Project ID " + requestDTO.getProjectId()
                                + " not found"));

        User assignedUser = null;

        // Agar new assignedUser diya gaya hai,
        // toh us User ko database se find karo
        if (requestDTO.getAssignedUserId() != null) {

            assignedUser = userRepository.findById(
                    requestDTO.getAssignedUserId()
            ).orElseThrow(() ->
                    new UserNotFoundException(
                            "User ID " + requestDTO.getAssignedUserId()
                                    + " not found"));
        }

        // Existing Task ke fields update kar rahe hain
        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setStatus(requestDTO.getStatus());
        task.setPriority(requestDTO.getPriority());
        task.setDueDate(requestDTO.getDueDate());

        // Relationships update kar rahe hain
        task.setProject(project);
        task.setAssignedUser(assignedUser);

        // Updated Task database mein save
        Task updatedTask = taskRepository.save(task);

        // Updated Entity ko Response DTO mein convert
        return taskMapper.mapToDto(updatedTask);
    }

    // Delete Task
    public void deleteTask(Long id) {

        // Pehle check karo Task exist karta hai
        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task ID " + id + " not found"));

        // Task database se delete
        taskRepository.delete(task);
    }
}