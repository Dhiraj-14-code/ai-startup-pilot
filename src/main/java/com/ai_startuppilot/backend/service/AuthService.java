package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.LoginRequestDTO;
import com.ai_startuppilot.backend.dto.LoginResponseDTO;
import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO requestDTO) {

        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                requestDTO.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponseDTO(token);
    }
}