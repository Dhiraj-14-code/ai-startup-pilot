package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.RiskRequestDTO;
import com.ai_startuppilot.backend.dto.RiskResponseDTO;
import com.ai_startuppilot.backend.service.RiskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risks")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    // Create Risk
    @PostMapping
    public ResponseEntity<RiskResponseDTO> createRisk(
            @Valid @RequestBody RiskRequestDTO requestDTO) {

        RiskResponseDTO responseDTO =
                riskService.createRisk(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDTO);
    }

    // Get all Risks
    @GetMapping
    public ResponseEntity<Page<RiskResponseDTO>> getAllRisks(Pageable pageable) {

        Page<RiskResponseDTO> responseDTO =
                riskService.getAllRisks(pageable);

        return ResponseEntity.ok(responseDTO);
    }

    // Get Risk by ID
    @GetMapping("/{id}")
    public ResponseEntity<RiskResponseDTO> getRiskById(
            @PathVariable Long id) {

        RiskResponseDTO responseDTO =
                riskService.getRiskById(id);

        return ResponseEntity.ok(responseDTO);
    }

    // Update Risk
    @PutMapping("/{id}")
    public ResponseEntity<RiskResponseDTO> updateRisk(
            @PathVariable Long id,
            @Valid @RequestBody RiskRequestDTO requestDTO) {

        RiskResponseDTO responseDTO =
                riskService.updateRisk(id, requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    // Delete Risk
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRisk(
            @PathVariable Long id) {

        riskService.deleteRisk(id);

        return ResponseEntity.noContent().build();
    }
}