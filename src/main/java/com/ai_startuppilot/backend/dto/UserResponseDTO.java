package com.ai_startuppilot.backend.dto;

import com.ai_startuppilot.backend.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
