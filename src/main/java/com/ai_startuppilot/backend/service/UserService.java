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


        public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
                this.userRepository = userRepository;
                this.userMapper = userMapper;
                this.passwordEncoder = passwordEncoder;
        }
        public UserResponseDTO registerUser(UserRequestDTO requestDTO){
                if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
                        throw new EmailAlreadyExists(
                                "Email already exists: " + requestDTO.getEmail()
                        );
                }                        User user = userMapper.mapToEntity(requestDTO);
                        user.setRole(UserRole.USER);
                        user.setPassword(
                                passwordEncoder.encode(requestDTO.getPassword())
                        );
                        User savedUser = userRepository.save(user);


                return userMapper.mapToDto(savedUser);

        }
}
