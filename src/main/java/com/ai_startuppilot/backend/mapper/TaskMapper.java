package com.ai_startuppilot.backend.mapper;

import com.ai_startuppilot.backend.dto.TaskRequestDTO;
import com.ai_startuppilot.backend.dto.TaskResponseDTO;
import com.ai_startuppilot.backend.entity.Task;
import com.ai_startuppilot.backend.entity.Project;
import com.ai_startuppilot.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    // DTO → Entity
    // Yahan project aur user already database se find hokar aayenge
    public Task mapToEntity(
            TaskRequestDTO dto,
            Project project,
            User assignedUser) {

        Task task = new Task();

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());

        // ID nahi, actual entity set kar rahe hain
        task.setProject(project);
        task.setAssignedUser(assignedUser);

        return task;
    }

    // Entity → Response DTO
    public TaskResponseDTO mapToDto(Task task) {

        TaskResponseDTO dto = new TaskResponseDTO();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());

        // Entity se sirf IDs response mein bhej rahe hain
        dto.setProjectId(task.getProject().getId());

        if (task.getAssignedUser() != null) {
            dto.setAssignedUserId(task.getAssignedUser().getId());
        }

        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        return dto;
    }
}