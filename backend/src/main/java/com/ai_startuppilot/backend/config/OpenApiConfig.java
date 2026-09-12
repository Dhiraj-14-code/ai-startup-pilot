package com.ai_startuppilot.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AI Startup Pilot API",
                version = "1.0",
                description = "API documentation for AI Startup Pilot backend"
        )
)
public class OpenApiConfig {
}
