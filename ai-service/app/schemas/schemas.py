from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from datetime import datetime

# --- Existing project management models ---
class TaskModel(BaseModel):
    id: int
    title: str
    status: str
    priority: str
    dueDate: Optional[datetime] = None
    assignedUserId: Optional[int] = None
    assignedUserName: Optional[str] = None

class MilestoneModel(BaseModel):
    id: int
    title: str
    status: str
    dueDate: Optional[datetime] = None

class RiskModel(BaseModel):
    id: int
    title: str
    severity: str
    status: str

# --- NEW: Business KPI models ---
class MonthlyKPI(BaseModel):
    month: str  # e.g. "2025-01", "2025-02"
    revenue: Optional[float] = 0
    expenses: Optional[float] = 0
    cashFlow: Optional[float] = None  # auto-calculated if missing
    customers: Optional[int] = 0
    churnRate: Optional[float] = None  # percentage
    burnRate: Optional[float] = None   # monthly burn

class BusinessData(BaseModel):
    runway_months: Optional[float] = None
    funding_raised: Optional[float] = 0
    funding_stage: Optional[str] = "Pre-Seed"
    industry: Optional[str] = "Technology"
    team_size: Optional[int] = 1
    monthly_kpis: List[MonthlyKPI] = []

# --- Updated request (backward-compatible) ---
class ProjectRequest(BaseModel):
    projectId: int
    projectName: str
    tasks: List[TaskModel] = []
    milestones: List[MilestoneModel] = []
    risks: List[RiskModel] = []
    businessData: Optional[BusinessData] = None

# --- Explainability models ---
class Explanation(BaseModel):
    factor: str
    impact: str          # "POSITIVE", "NEGATIVE", "NEUTRAL"
    score_impact: float  # how much it changed the score
    reason: str          # human-readable explanation

class RiskPrediction(BaseModel):
    risk: str
    probability: str     # "HIGH", "MEDIUM", "LOW"
    timeframe: str       # "1-3 months", "3-6 months", etc
    explanation: str

class KPITrend(BaseModel):
    metric: str
    direction: str       # "UP", "DOWN", "STABLE"
    change_pct: float
    interpretation: str

# --- Updated response ---
class AIAnalysisResponse(BaseModel):
    projectId: int
    projectName: str
    healthScore: float
    healthStatus: str
    taskCompletionRate: float
    overdueTasks: int
    totalMilestones: int
    completedMilestones: int
    openRisks: int
    criticalRisks: int
    warnings: List[str]
    recommendations: List[str]
    milestoneProgress: float
    
    # AI specific
    workloadAnalysis: Dict[str, Any]
    prediction: str
    insights: List[str]
    
    # NEW: Explainable AI & KPI fields
    explanations: List[Dict[str, Any]] = []
    riskPredictions: List[Dict[str, Any]] = []
    kpiTrends: List[Dict[str, Any]] = []
    kpiSummary: Dict[str, Any] = {}
