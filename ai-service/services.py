from datetime import datetime, timezone
from typing import List, Dict, Any
from schemas import ProjectRequest, AIAnalysisResponse

def analyze_project(req: ProjectRequest) -> AIAnalysisResponse:
    now = datetime.now(timezone.utc)
    
    # 1. Basic Stats
    total_tasks = len(req.tasks)
    completed_tasks = sum(1 for t in req.tasks if t.status == "COMPLETED")
    task_comp_rate = (completed_tasks / total_tasks * 100) if total_tasks > 0 else 100.0
    
    # Overdue task calculation
    # A task is overdue if its status is not COMPLETED (or CANCELLED - assuming we only have COMPLETED check)
    # and its due date is in the past.
    overdue_tasks = 0
    overdue_task_ids = []
    for t in req.tasks:
        if t.status != "COMPLETED" and t.dueDate and t.dueDate < now:
            overdue_tasks += 1
            overdue_task_ids.append(t.id)
            
    total_milestones = len(req.milestones)
    completed_milestones = sum(1 for m in req.milestones if m.status == "COMPLETED")
    milestone_progress = (completed_milestones / total_milestones * 100) if total_milestones > 0 else 100.0
    
    open_risks = sum(1 for r in req.risks if r.status != "CLOSED")
    critical_risks = sum(1 for r in req.risks if r.severity == "CRITICAL" and r.status != "CLOSED")
    
    # 2. Health Score Calculation (Align with Spring Boot)
    task_score = task_comp_rate * 0.40
    milestone_score = milestone_progress * 0.20
    overdue_penalty = min(overdue_tasks * 5, 20)
    risk_penalty = min((open_risks * 3) + (critical_risks * 7), 20)
    
    health_score = task_score + milestone_score + 20 - overdue_penalty - risk_penalty
    health_score = max(0.0, min(100.0, health_score))
    
    if health_score >= 80:
        health_status = "HEALTHY"
    elif health_score >= 60:
        health_status = "AT_RISK"
    elif health_score >= 40:
        health_status = "CRITICAL"
    else:
        health_status = "SEVERELY_CRITICAL"
        
    # 3. Workload Analysis
    workload: Dict[str, Any] = {}
    for t in req.tasks:
        if t.assignedUserId is not None:
            uid = str(t.assignedUserId)
            if uid not in workload:
                workload[uid] = {
                    "userId": t.assignedUserId,
                    "userName": t.assignedUserName or f"User {uid}",
                    "assigned": 0,
                    "completed": 0,
                    "pending": 0,
                    "overdue": 0
                }
            workload[uid]["assigned"] += 1
            if t.status == "COMPLETED":
                workload[uid]["completed"] += 1
            else:
                workload[uid]["pending"] += 1
                if t.dueDate and t.dueDate < now:
                    workload[uid]["overdue"] += 1
                    
    # 4. Warnings and Recommendations
    warnings = []
    recommendations = []
    insights = []
    
    # Absolute Business Rule Enforcement
    if overdue_tasks > 0:
        warnings.append(f"{overdue_tasks} task(s) are overdue.")
        recommendations.append("Prioritize the overdue tasks.")
        insights.append(f"[RULE_BASED] Detected {overdue_tasks} overdue tasks. Immediate action required.")
    
    if critical_risks > 0:
        warnings.append(f"{critical_risks} critical risk(s) are open.")
        recommendations.append("Resolve critical risks first.")
        insights.append(f"[RULE_BASED] Critical risks found. This heavily impacts project health.")
        
    if open_risks >= 3:
        warnings.append("Project has multiple open risks.")
        recommendations.append("Review and prioritize open risks.")
        
    if task_comp_rate < 50 and total_tasks > 0:
        warnings.append("Task completion rate is below 50%.")
        
    if milestone_progress < 50 and total_milestones > 0:
        warnings.append("Milestone progress is below 50%.")
        recommendations.append("Review milestone progress and deadlines.")
        
    if not recommendations:
        recommendations.append("Project is progressing normally.")
        
    # Workload imbalance insight
    if workload:
        avg_assigned = sum(w["assigned"] for w in workload.values()) / len(workload)
        for uid, w in workload.items():
            if w["assigned"] > avg_assigned * 1.5 and w["assigned"] > 3:
                insights.append(f"[STATISTICAL] Workload imbalance detected: {w['userName']} is handling significantly more tasks than average.")
                recommendations.append(f"Consider reassigning some tasks from {w['userName']} to balance workload.")
                break
                
    # 5. Prediction (Simple Statistical Fallback)
    if total_tasks > 0 and completed_tasks > 0:
        prediction = "[STATISTICAL] Based on current progress, project is moving forward."
    elif total_tasks == 0:
        prediction = "[RULE_BASED] No tasks assigned yet. Cannot predict completion."
    else:
        prediction = "[RULE_BASED] No tasks completed yet. Risk of significant delay."
        
    return AIAnalysisResponse(
        projectId=req.projectId,
        projectName=req.projectName,
        healthScore=health_score,
        healthStatus=health_status,
        taskCompletionRate=task_comp_rate,
        overdueTasks=overdue_tasks,
        totalMilestones=total_milestones,
        completedMilestones=completed_milestones,
        openRisks=open_risks,
        criticalRisks=critical_risks,
        warnings=warnings,
        recommendations=recommendations,
        milestoneProgress=milestone_progress,
        workloadAnalysis=workload,
        prediction=prediction,
        insights=insights
    )
