package com.ai_startuppilot.backend.controller;

import com.ai_startuppilot.backend.dto.UserRequestDTO;
import com.ai_startuppilot.backend.dto.UserResponseDTO;
import com.ai_startuppilot.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/register")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserRequestDTO requestDTO
            ){
        UserResponseDTO responseDTO = userService.registerUser(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDTO);
    }
}
