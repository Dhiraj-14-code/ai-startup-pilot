package com.ai_startuppilot.backend.mapper;

import com.ai_startuppilot.backend.dto.UserRequestDTO;
import com.ai_startuppilot.backend.dto.UserResponseDTO;
import com.ai_startuppilot.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    //dto to entity
    public User mapToEntity(UserRequestDTO requestDTO){
        User user = new User();

        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(requestDTO.getPassword());

        return user;
    }
    //Entity to dto
    public UserResponseDTO mapToDto(User user){
        UserResponseDTO responseDTO= new UserResponseDTO();

        responseDTO.setId(user.getId());
        responseDTO.setName(user.getName());
        responseDTO.setEmail(user.getEmail());
        responseDTO.setRole(user.getRole());
        responseDTO.setCreatedAt(user.getCreatedAt());
        responseDTO.setUpdatedAt(user.getUpdatedAt());

        return responseDTO;
    }
}
