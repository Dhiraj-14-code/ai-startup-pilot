from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from datetime import datetime

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

class ProjectRequest(BaseModel):
    projectId: int
    projectName: str
    tasks: List[TaskModel] = []
    milestones: List[MilestoneModel] = []
    risks: List[RiskModel] = []

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
    
    # AI specific additions
    workloadAnalysis: Dict[str, Any]
    prediction: str
    insights: List[str]
