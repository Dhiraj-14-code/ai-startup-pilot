package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.MilestoneRequestDTO;
import com.ai_startuppilot.backend.dto.MilestoneResponseDTO;
import com.ai_startuppilot.backend.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    // Create
    @PostMapping
    public ResponseEntity<MilestoneResponseDTO> createMilestone(
            @Valid @RequestBody MilestoneRequestDTO requestDTO) {

        MilestoneResponseDTO responseDTO =
                milestoneService.createMilestone(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDTO);
    }

    // Get all
    @GetMapping
    public ResponseEntity<Page<MilestoneResponseDTO>> getAllMilestones(Pageable pageable) {

        Page<MilestoneResponseDTO> responseDTO =
                milestoneService.getAllMilestones(pageable);

        return ResponseEntity.ok(responseDTO);
    }

    // Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<MilestoneResponseDTO> getMilestoneById(
            @PathVariable Long id) {

        MilestoneResponseDTO responseDTO =
                milestoneService.getMilestoneById(id);

        return ResponseEntity.ok(responseDTO);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<MilestoneResponseDTO> updateMilestone(
            @PathVariable Long id,
            @Valid @RequestBody MilestoneRequestDTO requestDTO) {

        MilestoneResponseDTO responseDTO =
                milestoneService.updateMilestone(id, requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMilestone(
            @PathVariable Long id) {

        milestoneService.deleteMilestone(id);

        return ResponseEntity.noContent().build();
    }
}