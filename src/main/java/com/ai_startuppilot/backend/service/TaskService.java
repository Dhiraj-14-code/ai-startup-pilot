package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.TaskRequestDTO;
import com.ai_startuppilot.backend.dto.TaskResponseDTO;
import com.ai_startuppilot.backend.dto.UserResponseDTO;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.Task;
import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.mapper.TaskMapper;
import com.ai_startuppilot.backend.repository.ProjectRepository;
import com.ai_startuppilot.backend.repository.TaskRepository;
import com.ai_startuppilot.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }
    public TaskResponseDTO createTask(TaskRequestDTO requestDTO) {

        Project project = projectRepository.findById(
                requestDTO.getProjectId()
        ).orElseThrow(() ->
                new RuntimeException("Project not found"));

        User assignedUser = null;

        if (requestDTO.getAssignedUserId() != null) {

            assignedUser = userRepository.findById(
                    requestDTO.getAssignedUserId()
            ).orElseThrow(() ->
                    new RuntimeException("User not found"));
        }

        Task task = taskMapper.mapToEntity(
                requestDTO,
                project,
                assignedUser
        );

        Task savedTask = taskRepository.save(task);

        return taskMapper.mapToDto(savedTask);
    }
}
