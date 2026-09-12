package com.ai_startuppilot.backend.security;

import com.ai_startuppilot.backend.dto.ProjectRequestDTO;
import com.ai_startuppilot.backend.enums.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests.
 * Uses the full Spring context with real security filter chain to verify:
 * - Unauthenticated requests to protected endpoints are rejected (401/403)
 * - Public endpoints (auth/login) are accessible without authentication
 * - Malformed JWT tokens are rejected
 *
 * NOTE: Role-based authorization (ADMIN vs USER) is not yet implemented.
 * Documented as a future requirement for Phase 6+.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    // Instantiate directly - com.fasterxml.jackson is not auto-configured as a
    // bean in Spring Boot 4.x (uses tools.jackson internally)
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ===== Protected endpoints reject unauthenticated requests =====
    @Test
    void getProjects_WithoutToken_ShouldReturn401Or403() throws Exception {
        int status = mockMvc().perform(get("/api/v1/projects"))
                .andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403,
                "Expected 401 or 403 for unauthenticated GET /projects, got: " + status);
    }

    @Test
    void getTasks_WithoutToken_ShouldReturn401Or403() throws Exception {
        int status = mockMvc().perform(get("/api/v1/tasks"))
                .andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403,
                "Expected 401 or 403 for unauthenticated GET /tasks, got: " + status);
    }

    @Test
    void createProject_WithoutToken_ShouldReturn401Or403() throws Exception {
        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("Unauthorized Project");
        request.setStatus(ProjectStatus.ACTIVE);

        int status = mockMvc().perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403,
                "Expected 401 or 403 for unauthenticated POST /projects, got: " + status);
    }

    // ===== Malformed/Invalid JWT token is rejected =====
    @Test
    void getProjects_WithMalformedToken_ShouldReturn401Or403() throws Exception {
        int status = mockMvc().perform(get("/api/v1/projects")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403,
                "Expected 401 or 403 for malformed JWT, got: " + status);
    }

    // ===== Public login endpoint is accessible (not blocked by security) =====
    @Test
    void loginEndpoint_ShouldBePublicAndNotReturn403() throws Exception {
        String loginBody = "{\"email\":\"nonexistent@example.com\",\"password\":\"wrongpass\"}";

        int status = mockMvc().perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn().getResponse().getStatus();

        // Must NOT be 403 (security block). Will be 401 (bad credentials from AuthService) or similar.
        assertNotEquals(403, status,
                "Login endpoint should be public and not return 403 Forbidden from security filter");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertNotEquals(int unexpected, int actual, String message) {
        if (unexpected == actual) throw new AssertionError(message);
    }
}
