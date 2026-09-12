package com.ai_startuppilot.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamRequestDTO {
    @NotBlank(message = "Team name is required")
    @Size(min = 3 , max = 100 ,
    message = "Team name must be between 3 to 100 characters")
    private String name;

    @Size(max = 500,
    message = "Description cannot exceed 500 characters")
    private String description;

}
