# PROJECT CONTEXT

## StartupPilot Platform
StartupPilot is an intelligent web platform designed to analyze startup project data and provide actionable AI-driven health scores, predictions, and recommendations.

## Architecture
The platform operates on a three-tier microservices-style architecture:

1. **backend**: A Java Spring Boot application using MySQL/H2 to handle authentication, project storage, and routing of analysis requests.
2. **ai-service**: A Python FastAPI service responsible for ingesting files, performing data parsing, predicting risk, and generating recommendations.
3. **frontend**: A React 19 / Vite client that presents a glassmorphic dashboard for file upload and project management.

## Completed Phases (1-5)
- Basic Java Spring Boot backend structure and project entities setup.
- Basic Python FastAPI service for mock AI insights.
- Basic React frontend dashboard for viewing projects.
- UI enhancements (glassmorphism, micro-animations, etc.).
- Clean monorepo restructuring separating the three layers.

## Remaining Work
- Implement actual AI logic, Risk Engine, and ML models in `ai-service`.
- Enhance frontend functionality with complex data visualizations.
- Add comprehensive unit and integration tests across all tiers.
- Set up Docker and containerization.
