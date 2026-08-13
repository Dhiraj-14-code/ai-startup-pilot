package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.TaskRequestDTO;
import com.ai_startuppilot.backend.dto.TaskResponseDTO;
import com.ai_startuppilot.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskRequestDTO requestDTO) {

        TaskResponseDTO responseDTO =
                taskService.createTask(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDTO);
    }
}