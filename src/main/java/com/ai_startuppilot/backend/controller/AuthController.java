package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.LoginRequestDTO;
import com.ai_startuppilot.backend.dto.LoginResponseDTO;
import com.ai_startuppilot.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO requestDTO
            ){
        LoginResponseDTO responseDTO = authService.login(requestDTO);

        return ResponseEntity.ok(responseDTO);
    }
}
