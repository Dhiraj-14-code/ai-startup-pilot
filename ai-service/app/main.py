from fastapi import FastAPI, UploadFile, File, HTTPException, Body
from fastapi.middleware.cors import CORSMiddleware
from app.schemas.schemas import (
    ProjectRequest, AIAnalysisResponse, TaskModel, MilestoneModel,
    RiskModel, BusinessData, MonthlyKPI
)
from app.services.services import analyze_project, analyze_business_kpis
import json
import pandas as pd
import io

app = FastAPI(title="AI StartupPilot Analysis Service", version="2.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ────────────────────────────────────────────────
#  EXISTING: Project Analysis (tasks/milestones)
# ────────────────────────────────────────────────
@app.post("/api/v1/analyze/project", response_model=AIAnalysisResponse)
async def analyze_project_endpoint(request: ProjectRequest):
    return analyze_project(request)


# ────────────────────────────────────────────────
#  NEW: Startup Business KPI Analysis (JSON body)
# ────────────────────────────────────────────────
@app.post("/api/v1/analyze/startup")
async def analyze_startup_endpoint(data: BusinessData):
    """Accept BusinessData JSON directly and return full KPI analysis."""
    result = analyze_business_kpis(data)
    return result


# ────────────────────────────────────────────────
#  FILE UPLOAD: auto-routes to correct analyzer
# ────────────────────────────────────────────────
_BUSINESS_COLUMNS = {"revenue", "expenses", "cashflow", "customers", "churn_rate",
                     "cash_flow", "churn", "month", "mrr", "arr", "burn_rate"}


@app.post("/api/v1/analyze/file", response_model=AIAnalysisResponse)
async def analyze_file_endpoint(file: UploadFile = File(...)):
    filename = file.filename.lower()
    content = await file.read()

    req = ProjectRequest(projectId=0, projectName=file.filename, tasks=[], milestones=[], risks=[])

    try:
        if filename.endswith(".json"):
            data = json.loads(content)
            # Check if it's a BusinessData payload
            if "monthly_kpis" in data or "funding_raised" in data:
                biz = BusinessData(**data)
                kpi_result = analyze_business_kpis(biz)
                req.businessData = biz
                return _kpi_result_to_response(req, kpi_result)
            req = ProjectRequest(**data)

        elif filename.endswith(".csv"):
            df = pd.read_csv(io.BytesIO(content))
            if _is_business_dataframe(df):
                biz = _parse_business_dataframe(df)
                req.businessData = biz
                kpi_result = analyze_business_kpis(biz)
                return _kpi_result_to_response(req, kpi_result)
            else:
                _parse_dataframe(df, req)

        elif filename.endswith(".xlsx"):
            df = pd.read_excel(io.BytesIO(content))
            if _is_business_dataframe(df):
                biz = _parse_business_dataframe(df)
                req.businessData = biz
                kpi_result = analyze_business_kpis(biz)
                return _kpi_result_to_response(req, kpi_result)
            else:
                _parse_dataframe(df, req)

        elif filename.endswith(".pdf"):
            import pypdf
            reader = pypdf.PdfReader(io.BytesIO(content))
            text = ""
            for page in reader.pages:
                text += page.extract_text() + "\n"
            lines = text.split('\n')
            for i, line in enumerate(lines):
                if line.strip():
                    req.tasks.append(TaskModel(
                        id=i, title=line[:50], status="PENDING", priority="MEDIUM"
                    ))
        else:
            raise HTTPException(status_code=400, detail="Unsupported file format")

        return analyze_project(req)

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=422, detail=f"Error parsing file: {str(e)}")


# ────────────────────────────────────────────────
#  HELPERS
# ────────────────────────────────────────────────
def _is_business_dataframe(df: pd.DataFrame) -> bool:
    cols = {c.lower().strip().replace(" ", "_") for c in df.columns}
    return bool(cols & _BUSINESS_COLUMNS)


def _parse_business_dataframe(df: pd.DataFrame) -> BusinessData:
    df.columns = [c.lower().strip().replace(" ", "_") for c in df.columns]
    kpis = []
    for _, row in df.iterrows():
        month = str(row.get("month", "")).strip() or f"Month {_ + 1}"
        kpis.append(MonthlyKPI(
            month=month,
            revenue=_safe_float(row.get("revenue", row.get("mrr", 0))),
            expenses=_safe_float(row.get("expenses", row.get("burn_rate", 0))),
            cashFlow=_safe_float(row.get("cash_flow", row.get("cashflow", None))),
            customers=_safe_int(row.get("customers", 0)),
            churnRate=_safe_float(row.get("churn_rate", row.get("churn", None))),
            burnRate=_safe_float(row.get("burn_rate", None)),
        ))
    return BusinessData(monthly_kpis=kpis)


def _kpi_result_to_response(req: ProjectRequest, kpi: dict) -> AIAnalysisResponse:
    score = kpi["kpi_score"]
    if score >= 80:
        status = "HEALTHY"
    elif score >= 60:
        status = "AT_RISK"
    elif score >= 40:
        status = "CRITICAL"
    else:
        status = "SEVERELY_CRITICAL"

    summary = kpi.get("kpi_summary", {})
    insights = [f"[KPI] {e['factor']}: {e['reason']}" for e in kpi.get("explanations", [])]

    if score >= 80:
        prediction = "[STARTUP_AI] Metrics look healthy. Maintain trajectory and focus on scale."
    elif score >= 60:
        prediction = "[STARTUP_AI] Some risks detected. Address warnings before they escalate."
    elif score >= 40:
        prediction = "[STARTUP_AI] Critical issues require immediate founder attention."
    else:
        prediction = "[STARTUP_AI] Startup is in severe distress. Emergency action required."

    return AIAnalysisResponse(
        projectId=req.projectId,
        projectName=req.projectName,
        healthScore=score,
        healthStatus=status,
        taskCompletionRate=100.0,
        overdueTasks=0,
        totalMilestones=0,
        completedMilestones=0,
        openRisks=len([r for r in kpi.get("risk_predictions", []) if r["probability"] in ("HIGH", "MEDIUM")]),
        criticalRisks=len([r for r in kpi.get("risk_predictions", []) if r["probability"] == "HIGH"]),
        warnings=kpi.get("warnings", []),
        recommendations=kpi.get("recommendations", []),
        milestoneProgress=100.0,
        workloadAnalysis={},
        prediction=prediction,
        insights=insights,
        explanations=kpi.get("explanations", []),
        riskPredictions=kpi.get("risk_predictions", []),
        kpiTrends=kpi.get("kpi_trends", []),
        kpiSummary=kpi.get("kpi_summary", {}),
    )


def _parse_dataframe(df, req: ProjectRequest):
    for index, row in df.iterrows():
        title = row.get("title", row.get("Task Name", f"Task {index}"))
        status = row.get("status", row.get("Status", "PENDING"))
        priority = row.get("priority", row.get("Priority", "MEDIUM"))
        req.tasks.append(TaskModel(
            id=index,
            title=str(title),
            status=str(status).upper() if pd.notnull(status) else "PENDING",
            priority=str(priority).upper() if pd.notnull(priority) else "MEDIUM",
            dueDate=None
        ))


def _safe_float(val) -> Optional[float]:
    try:
        return float(val) if val is not None and pd.notnull(val) else None
    except Exception:
        return None


def _safe_int(val) -> Optional[int]:
    try:
        return int(val) if val is not None and pd.notnull(val) else None
    except Exception:
        return None


from typing import Optional
