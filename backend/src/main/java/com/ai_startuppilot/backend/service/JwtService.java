package com.ai_startuppilot.backend.service;

import com.ai_startuppilot.backend.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

    @Service
    public class JwtService {

        private final String secret;

        public JwtService(@Value("${jwt.secret}") String secret) {
            this.secret = secret;
        }

        private SecretKey getSigningKey() {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

            return Keys.hmacShaKeyFor(keyBytes);
        }

        public String generateToken(User user) {

            return Jwts.builder()
                    .subject(user.getEmail())
                    .claim("role", user.getRole().name())
                    .issuedAt(new Date())
                    .expiration(new Date(
                            System.currentTimeMillis() + 1000 * 60 * 60
                    ))
                    .signWith(getSigningKey())
                    .compact();
        }
        public String extractUsername(String token) {

            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        }
        public boolean isTokenValid(String token, User user) {

            String username = extractUsername(token);

            return username.equals(user.getEmail());
        }
    }