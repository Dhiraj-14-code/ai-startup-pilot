package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.dto.LoginRequestDTO;
import com.ai_startuppilot.backend.dto.LoginResponseDTO;
import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.exception.InvalidCredentialsException;
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

    // Login User
    public LoginResponseDTO login(LoginRequestDTO requestDTO) {

        // Email se user find karo
        User user = userRepository.findByEmail(
                requestDTO.getEmail()
        ).orElseThrow(() ->
                new InvalidCredentialsException(
                        "Invalid email or password"
                ));

        // Request ka raw password aur database ka
        // hashed password compare kar rahe hain
        boolean passwordMatches = passwordEncoder.matches(
                requestDTO.getPassword(),
                user.getPassword()
        );

        // Password incorrect hai
        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        // Credentials correct hain → JWT generate karo
        String token = jwtService.generateToken(user);

        return new LoginResponseDTO(token);
    }
}