package com.ai_startuppilot.backend.security;

import com.ai_startuppilot.backend.entity.User;
import com.ai_startuppilot.backend.repository.UserRepository;
import com.ai_startuppilot.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);//The actual JWT starts after those 7 characters.
        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(username).orElse(null);
            
            if (user != null && jwtService.isTokenValid(token, user)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.emptyList()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Token is invalid, expired, or malformed.
            // Do nothing, leave the SecurityContext empty.
            // Spring Security will automatically return 401/403 for protected routes.
        }

        filterChain.doFilter(request,response);
        //"I have finished processing this request. Continue to the next filter/controller."
    }
}
