import pytest
from fastapi.testclient import TestClient
from main import app
from schemas import ProjectRequest, TaskModel, RiskModel, MilestoneModel
from datetime import datetime, timedelta, timezone

client = TestClient(app)

def test_analyze_project_zero_overdue_rule():
    req = ProjectRequest(
        projectId=1,
        projectName="Test",
        tasks=[
            TaskModel(id=1, title="Done Task", status="COMPLETED", priority="HIGH", dueDate=datetime.now(timezone.utc) - timedelta(days=1)),
            TaskModel(id=2, title="Future Task", status="PENDING", priority="LOW", dueDate=datetime.now(timezone.utc) + timedelta(days=1))
        ]
    )
    
    response = client.post("/api/v1/analyze/project", json=req.model_dump(mode='json'))
    assert response.status_code == 200
    data = response.json()
    
    # zero-overdue rule
    assert data["overdueTasks"] == 0
    
    # no overdue warning or recommendation
    warnings_str = " ".join(data["warnings"]).lower()
    recs_str = " ".join(data["recommendations"]).lower()
    
    assert "overdue" not in warnings_str
    assert "overdue" not in recs_str

def test_analyze_project_overdue_detection():
    req = ProjectRequest(
        projectId=1,
        projectName="Test",
        tasks=[
            TaskModel(id=1, title="Late Task", status="PENDING", priority="HIGH", dueDate=datetime.now(timezone.utc) - timedelta(days=1))
        ]
    )
    
    response = client.post("/api/v1/analyze/project", json=req.model_dump(mode='json'))
    assert response.status_code == 200
    data = response.json()
    
    assert data["overdueTasks"] == 1
    warnings_str = " ".join(data["warnings"]).lower()
    assert "overdue" in warnings_str

def test_health_score_calculation():
    req = ProjectRequest(
        projectId=1,
        projectName="Test",
        tasks=[
            TaskModel(id=1, title="Done", status="COMPLETED", priority="HIGH")
        ],
        milestones=[
            MilestoneModel(id=1, title="Done Milestone", status="COMPLETED")
        ],
        risks=[]
    )
    
    response = client.post("/api/v1/analyze/project", json=req.model_dump(mode='json'))
    data = response.json()
    
    assert data["healthScore"] == 100.0
    assert data["healthStatus"] == "HEALTHY"

def test_workload_analysis():
    req = ProjectRequest(
        projectId=1,
        projectName="Test",
        tasks=[
            TaskModel(id=1, title="T1", status="PENDING", priority="LOW", assignedUserId=100, assignedUserName="Alice"),
            TaskModel(id=2, title="T2", status="COMPLETED", priority="LOW", assignedUserId=100, assignedUserName="Alice")
        ]
    )
    
    response = client.post("/api/v1/analyze/project", json=req.model_dump(mode='json'))
    data = response.json()
    
    workload = data["workloadAnalysis"]
    assert "100" in workload
    assert workload["100"]["assigned"] == 2
    assert workload["100"]["completed"] == 1
    assert workload["100"]["pending"] == 1

def test_risk_detection():
    req = ProjectRequest(
        projectId=1,
        projectName="Test",
        risks=[
            RiskModel(id=1, title="Critical Issue", severity="CRITICAL", status="OPEN")
        ]
    )
    response = client.post("/api/v1/analyze/project", json=req.model_dump(mode='json'))
    data = response.json()
    
    assert data["criticalRisks"] == 1
    assert data["openRisks"] == 1
    assert "CRITICAL" in data["healthStatus"] or "AT_RISK" in data["healthStatus"]

def test_prediction_fallback():
    req = ProjectRequest(projectId=1, projectName="Test")
    response = client.post("/api/v1/analyze/project", json=req.model_dump(mode='json'))
    data = response.json()
    
    assert "prediction" in data
    assert "[RULE_BASED]" in data["prediction"] or "[STATISTICAL]" in data["prediction"]
