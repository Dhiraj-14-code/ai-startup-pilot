# AI Startup Pilot

AI Startup Pilot is an intelligent web platform designed to analyze startup project data and provide actionable AI-driven health scores, predictions, and recommendations. 

## 🏗 Project Architecture

The project is built using a modern microservices-style architecture, separated into three main layers:

### 1. Frontend Client
- **Tech Stack**: React 19, Vite, React Router, Axios.
- **Features**: 
  - Dynamic Dashboard to view all active projects.
  - Interactive file upload section supporting `.json`, `.csv`, `.xlsx`, and `.pdf` files for AI analysis.
  - Premium glassmorphic UI with vibrant micro-animations.
- **Directory**: `/frontend`

### 2. Core Backend Service (Java/Spring Boot)
- **Tech Stack**: Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA, MySQL/H2.
- **Features**: 
  - RESTful API handling project data, authentication, and user sessions.
  - Acts as the primary gateway, securely routing analysis requests to the Python AI service.
  - Swagger/OpenAPI documentation included.
- **Directory**: `/` (Root directory, built with Maven)

### 3. AI Analysis Microservice (Python)
- **Tech Stack**: Python, FastAPI (assumed from standard `main.py` & `schemas.py` structure).
- **Features**: 
  - Dedicated service for heavy data processing and AI inference.
  - Parses uploaded datasets and returns structured insights:
    - **Health Score**: A numerical evaluation of the startup's current health.
    - **Health Status**: E.g., Healthy, At Risk, Critical.
    - **Prediction**: AI forecast based on the ingested data.
    - **Recommendations**: Actionable steps to improve the startup's trajectory.
- **Directory**: `/ai-service`

## 🚀 Getting Started

### Prerequisites
- Node.js & npm (for Frontend)
- Java 21 & Maven (for Core Backend)
- Python 3.10+ (for AI Service)
- MySQL (Optional, if not using H2 in-memory DB)

### Running the Application Locally

1. **Start the Core Backend (Spring Boot)**
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Ensure your database configurations in `application.properties`/`application.yml` are correct).*

2. **Start the AI Service (Python)**
   We recommend using a virtual environment.

   **On Windows (PowerShell):**
   ```powershell
   cd ai-service
   python -m venv venv
   .\venv\Scripts\activate
   pip install -r requirements.txt
   uvicorn main:app --reload
   ```

   **On macOS/Linux:**
   ```bash
   cd ai-service
   python3 -m venv venv
   .\venv\Scripts\activate for activate venv
   pip install -r requirements.txt
   uvicorn main:app --reload
   ```

3. **Start the Frontend Client (React/Vite)**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The frontend will be available at `http://localhost:5173`.

## 🛡 Authentication & Security
The backend utilizes JSON Web Tokens (JWT) for securing endpoints. Ensure you provide valid authentication headers when interacting with the API via Postman or the frontend client.

## 🤝 Contributing
Contributions, issues, and feature requests are welcome! Feel free to check the issues page.