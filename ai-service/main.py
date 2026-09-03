from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from schemas import ProjectRequest, AIAnalysisResponse, TaskModel, MilestoneModel, RiskModel
from services import analyze_project
import json
import pandas as pd
import io

app = FastAPI(title="AI StartupPilot Analysis Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/api/v1/analyze/project", response_model=AIAnalysisResponse)
async def analyze_project_endpoint(request: ProjectRequest):
    return analyze_project(request)

@app.post("/api/v1/analyze/file", response_model=AIAnalysisResponse)
async def analyze_file_endpoint(file: UploadFile = File(...)):
    filename = file.filename.lower()
    content = await file.read()
    
    req = ProjectRequest(projectId=0, projectName=file.filename, tasks=[], milestones=[], risks=[])
    
    try:
        if filename.endswith(".json"):
            data = json.loads(content)
            req = ProjectRequest(**data)
        elif filename.endswith(".csv"):
            df = pd.read_csv(io.BytesIO(content))
            _parse_dataframe(df, req)
        elif filename.endswith(".xlsx"):
            df = pd.read_excel(io.BytesIO(content))
            _parse_dataframe(df, req)
        elif filename.endswith(".pdf"):
            import pypdf
            reader = pypdf.PdfReader(io.BytesIO(content))
            text = ""
            for page in reader.pages:
                text += page.extract_text() + "\n"
            
            # Very basic unstructured text parsing for MVP
            lines = text.split('\n')
            for i, line in enumerate(lines):
                if line.strip():
                    req.tasks.append(TaskModel(
                        id=i, title=line[:50], status="PENDING", priority="MEDIUM"
                    ))
        else:
            raise HTTPException(status_code=400, detail="Unsupported file format")
            
        return analyze_project(req)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Error parsing file: {str(e)}")

def _parse_dataframe(df, req: ProjectRequest):
    # A simple normalizer that looks for standard columns
    for index, row in df.iterrows():
        # Minimal extraction for MVP
        title = row.get("title", row.get("Task Name", f"Task {index}"))
        status = row.get("status", row.get("Status", "PENDING"))
        priority = row.get("priority", row.get("Priority", "MEDIUM"))
        
        req.tasks.append(
            TaskModel(
                id=index,
                title=str(title),
                status=str(status).upper() if pd.notnull(status) else "PENDING",
                priority=str(priority).upper() if pd.notnull(priority) else "MEDIUM",
                dueDate=None
            )
        )
