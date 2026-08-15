package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.UserRequestDTO;
import com.ai_startuppilot.backend.dto.UserResponseDTO;
import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.enums.UserRole;
import com.ai_startuppilot.backend.exception.EmailAlreadyExists;
import com.ai_startuppilot.backend.mapper.UserMapper;
import com.ai_startuppilot.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final PasswordEncoder passwordEncoder;

        public UserService(
                UserRepository userRepository,
                UserMapper userMapper,
                PasswordEncoder passwordEncoder) {

                this.userRepository = userRepository;
                this.userMapper = userMapper;
                this.passwordEncoder = passwordEncoder;
        }

        // Register User
        public UserResponseDTO registerUser(
                UserRequestDTO requestDTO) {

                // Check karo email already registered hai ya nahi
                if (userRepository.findByEmail(
                        requestDTO.getEmail()).isPresent()) {

                        throw new EmailAlreadyExists(
                                "Email already exists: "
                                        + requestDTO.getEmail()
                        );
                }

                // DTO → Entity
                User user = userMapper.mapToEntity(requestDTO);

                // Default role USER set kar rahe hain
                user.setRole(UserRole.USER);

                // Password ko hash karke database mein save karenge
                user.setPassword(
                        passwordEncoder.encode(
                                requestDTO.getPassword()
                        )
                );

                // Database mein save
                User savedUser = userRepository.save(user);

                // Entity → Response DTO
                return userMapper.mapToDto(savedUser);
        }
}