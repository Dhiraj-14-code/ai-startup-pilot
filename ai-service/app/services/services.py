from datetime import datetime, timezone
from typing import List, Dict, Any, Optional
from app.schemas.schemas import ProjectRequest, AIAnalysisResponse, BusinessData, MonthlyKPI


# ─────────────────────────────────────────────────────────────
#  BUSINESS KPI ENGINE
# ─────────────────────────────────────────────────────────────

def _safe_pct_change(new: float, old: float) -> float:
    if old == 0:
        return 0.0
    return round((new - old) / abs(old) * 100, 2)


def analyze_business_kpis(biz: BusinessData) -> Dict[str, Any]:
    kpis = biz.monthly_kpis

    # Auto-fill cashflow
    for k in kpis:
        if k.cashFlow is None:
            k.cashFlow = (k.revenue or 0) - (k.expenses or 0)

    # ── KPI Trends ─────────────────────────────────────────
    kpi_trends = []
    if len(kpis) >= 2:
        prev, curr = kpis[-2], kpis[-1]
        rev_chg = _safe_pct_change(curr.revenue or 0, prev.revenue or 0)
        exp_chg = _safe_pct_change(curr.expenses or 0, prev.expenses or 0)
        cf_chg  = _safe_pct_change(curr.cashFlow or 0, prev.cashFlow or 0)
        cust_chg = _safe_pct_change(curr.customers or 0, prev.customers or 0)

        kpi_trends.append({"metric": "Revenue", "direction": "UP" if rev_chg > 2 else ("DOWN" if rev_chg < -2 else "STABLE"),
                           "change_pct": rev_chg, "interpretation": _trend_text("Revenue", rev_chg)})
        kpi_trends.append({"metric": "Expenses", "direction": "UP" if exp_chg > 2 else ("DOWN" if exp_chg < -2 else "STABLE"),
                           "change_pct": exp_chg, "interpretation": _trend_text("Expenses", exp_chg, invert=True)})
        kpi_trends.append({"metric": "Cash Flow", "direction": "UP" if cf_chg > 2 else ("DOWN" if cf_chg < -2 else "STABLE"),
                           "change_pct": cf_chg, "interpretation": _trend_text("Cash Flow", cf_chg)})
        kpi_trends.append({"metric": "Customer Growth", "direction": "UP" if cust_chg > 2 else ("DOWN" if cust_chg < -2 else "STABLE"),
                           "change_pct": cust_chg, "interpretation": _trend_text("Customer Growth", cust_chg)})

    # ── KPI Summary ────────────────────────────────────────
    total_rev  = sum(k.revenue or 0 for k in kpis)
    total_exp  = sum(k.expenses or 0 for k in kpis)
    avg_burn   = total_exp / len(kpis) if kpis else 0
    latest_cf  = kpis[-1].cashFlow if kpis else 0
    latest_rev = kpis[-1].revenue  if kpis else 0
    latest_exp = kpis[-1].expenses if kpis else 0
    latest_cust= kpis[-1].customers if kpis else 0
    latest_churn = kpis[-1].churnRate if kpis else None

    # Runway calculation
    cash_balance = biz.funding_raised or 0
    for k in kpis:
        cash_balance += (k.cashFlow or 0)
    runway = (cash_balance / avg_burn) if avg_burn > 0 else None
    if biz.runway_months is not None:
        runway = biz.runway_months  # explicit override

    kpi_summary = {
        "total_revenue": total_rev,
        "total_expenses": total_exp,
        "avg_monthly_burn": round(avg_burn, 2),
        "latest_cashflow": latest_cf,
        "latest_revenue": latest_rev,
        "latest_expenses": latest_exp,
        "latest_customers": latest_cust,
        "latest_churn_rate": latest_churn,
        "runway_months": round(runway, 1) if runway is not None else None,
        "funding_stage": biz.funding_stage,
        "industry": biz.industry,
        "team_size": biz.team_size,
    }

    # ── KPI Health Score (0–100) ───────────────────────────
    kpi_score = 60.0  # base
    explanations = []

    # Factor 1: Revenue trend
    if len(kpi_trends) >= 1:
        rev_trend = kpi_trends[0]
        if rev_trend["direction"] == "UP":
            delta = min(rev_trend["change_pct"] / 10, 15)
            kpi_score += delta
            explanations.append({"factor": "Revenue Growth", "impact": "POSITIVE",
                                  "score_impact": round(delta, 1),
                                  "reason": f"Revenue grew {rev_trend['change_pct']:.1f}% MoM — strong demand signal."})
        elif rev_trend["direction"] == "DOWN":
            delta = min(abs(rev_trend["change_pct"]) / 10, 20)
            kpi_score -= delta
            explanations.append({"factor": "Revenue Decline", "impact": "NEGATIVE",
                                  "score_impact": round(-delta, 1),
                                  "reason": f"Revenue fell {abs(rev_trend['change_pct']):.1f}% MoM — needs immediate attention."})
        else:
            explanations.append({"factor": "Revenue Stability", "impact": "NEUTRAL", "score_impact": 0.0,
                                  "reason": "Revenue is stable — focus on growth acceleration."})

    # Factor 2: Runway
    if runway is not None:
        if runway < 2:
            kpi_score -= 25
            explanations.append({"factor": "Cash Runway", "impact": "NEGATIVE", "score_impact": -25.0,
                                  "reason": f"Only {runway:.1f} months of runway left — critical cash crisis imminent."})
        elif runway < 4:
            kpi_score -= 15
            explanations.append({"factor": "Cash Runway", "impact": "NEGATIVE", "score_impact": -15.0,
                                  "reason": f"{runway:.1f} months runway is dangerously low. Begin fundraising immediately."})
        elif runway < 9:
            kpi_score -= 5
            explanations.append({"factor": "Cash Runway", "impact": "NEGATIVE", "score_impact": -5.0,
                                  "reason": f"{runway:.1f} months runway — healthy short-term but plan Series A/bridge."})
        else:
            kpi_score += 10
            explanations.append({"factor": "Cash Runway", "impact": "POSITIVE", "score_impact": 10.0,
                                  "reason": f"{runway:.1f} months runway — strong financial position."})

    # Factor 3: Churn rate
    if latest_churn is not None:
        if latest_churn > 10:
            delta = min((latest_churn - 10) * 1.5, 20)
            kpi_score -= delta
            explanations.append({"factor": "Churn Rate", "impact": "NEGATIVE", "score_impact": round(-delta, 1),
                                  "reason": f"Churn rate at {latest_churn:.1f}% — far above acceptable 5%. Customer retention crisis."})
        elif latest_churn > 5:
            kpi_score -= 8
            explanations.append({"factor": "Churn Rate", "impact": "NEGATIVE", "score_impact": -8.0,
                                  "reason": f"Churn rate {latest_churn:.1f}% is above the 5% healthy threshold."})
        else:
            kpi_score += 5
            explanations.append({"factor": "Churn Rate", "impact": "POSITIVE", "score_impact": 5.0,
                                  "reason": f"Churn rate {latest_churn:.1f}% — healthy retention."})

    # Factor 4: Cashflow positivity
    if latest_cf is not None:
        if latest_cf > 0:
            kpi_score += 8
            explanations.append({"factor": "Cash Flow", "impact": "POSITIVE", "score_impact": 8.0,
                                  "reason": f"Positive cash flow of ${latest_cf:,.0f} — business is self-sustaining."})
        elif latest_cf < -avg_burn * 0.5:
            kpi_score -= 10
            explanations.append({"factor": "Cash Flow", "impact": "NEGATIVE", "score_impact": -10.0,
                                  "reason": f"Cash burn is accelerating. Current burn ${abs(latest_cf):,.0f} exceeds average."})

    # Factor 5: Customer growth
    if len(kpi_trends) >= 4:
        cust_trend = kpi_trends[3]
        if cust_trend["direction"] == "UP":
            delta = min(cust_trend["change_pct"] / 5, 10)
            kpi_score += delta
            explanations.append({"factor": "Customer Growth", "impact": "POSITIVE", "score_impact": round(delta, 1),
                                  "reason": f"Customer base grew {cust_trend['change_pct']:.1f}% — strong product-market fit signal."})
        elif cust_trend["direction"] == "DOWN":
            kpi_score -= 8
            explanations.append({"factor": "Customer Growth", "impact": "NEGATIVE", "score_impact": -8.0,
                                  "reason": f"Customer count declining {abs(cust_trend['change_pct']):.1f}% — review acquisition channels."})

    kpi_score = max(0.0, min(100.0, kpi_score))

    # ── Risk Predictions ───────────────────────────────────
    risk_predictions = []

    if runway is not None and runway < 4:
        risk_predictions.append({
            "risk": "Cash Crunch / Insolvency",
            "probability": "HIGH" if runway < 2 else "MEDIUM",
            "timeframe": f"Within {max(1, int(runway))} months",
            "explanation": f"At current burn rate of ${avg_burn:,.0f}/month with {runway:.1f} months runway, cash will be depleted without immediate action. Raise a bridge round or cut burn by 30%."
        })

    if latest_churn is not None and latest_churn > 7:
        risk_predictions.append({
            "risk": "Churn Escalation",
            "probability": "HIGH" if latest_churn > 12 else "MEDIUM",
            "timeframe": "1–3 months",
            "explanation": f"Churn at {latest_churn:.1f}% compounding will erode your customer base significantly. Implement retention programs and analyze exit surveys immediately."
        })

    if len(kpis) >= 3:
        recent_revs = [k.revenue or 0 for k in kpis[-3:]]
        if recent_revs[-1] < recent_revs[0] * 0.85:
            risk_predictions.append({
                "risk": "Revenue Plateau / Decline",
                "probability": "HIGH",
                "timeframe": "1–3 months",
                "explanation": "Revenue has declined over the past 3 months. Review pricing, churn causes, and sales pipeline immediately."
            })
        elif recent_revs[-1] < recent_revs[0] * 1.02:
            risk_predictions.append({
                "risk": "Growth Stagnation",
                "probability": "MEDIUM",
                "timeframe": "3–6 months",
                "explanation": "Revenue growth is flat. Without action, you risk missing funding milestones and losing competitive ground."
            })

    if len(kpis) >= 2:
        exp_growth = _safe_pct_change(kpis[-1].expenses or 0, kpis[-2].expenses or 0)
        rev_growth = _safe_pct_change(kpis[-1].revenue or 0, kpis[-2].revenue or 0)
        if exp_growth > rev_growth + 10:
            risk_predictions.append({
                "risk": "Burn Rate Acceleration",
                "probability": "MEDIUM",
                "timeframe": "2–4 months",
                "explanation": f"Expenses growing {exp_growth:.1f}% while revenue grows {rev_growth:.1f}%. Cost structure is expanding faster than revenue — audit all discretionary spend."
            })

    if not risk_predictions:
        risk_predictions.append({
            "risk": "No Critical Risks Detected",
            "probability": "LOW",
            "timeframe": "6+ months",
            "explanation": "Business metrics look healthy. Continue monitoring KPIs monthly and maintain current trajectory."
        })

    # ── Recommendations ────────────────────────────────────
    recommendations = []
    warnings = []

    for rp in risk_predictions:
        if rp["probability"] == "HIGH":
            warnings.append(f"🚨 HIGH RISK: {rp['risk']} — {rp['timeframe']}")
        elif rp["probability"] == "MEDIUM":
            warnings.append(f"⚠️ MEDIUM RISK: {rp['risk']} — {rp['timeframe']}")

    if runway is not None:
        if runway < 4:
            recommendations.append("🔴 URGENT: Begin fundraising or reduce burn rate immediately. Target 12+ months runway.")
        elif runway < 9:
            recommendations.append("🟡 Start preparing your next funding round deck — 9–12 months runway is the sweet spot to raise.")
        else:
            recommendations.append("🟢 Strong runway position. Focus on growth and scaling operations.")

    if latest_churn is not None and latest_churn > 5:
        recommendations.append(f"Implement a customer success program to reduce churn from {latest_churn:.1f}% to below 5%.")
        recommendations.append("Analyze your top 10% churned customers — identify and fix the #1 exit reason.")

    if len(kpi_trends) >= 1 and kpi_trends[0]["direction"] == "DOWN":
        recommendations.append("Revenue is declining — conduct win/loss analysis and revisit your ICP (Ideal Customer Profile).")
        recommendations.append("Consider expanding into adjacent market segments or launching a new pricing tier.")

    if len(kpi_trends) >= 2 and kpi_trends[1]["direction"] == "UP":
        recommendations.append("Expenses are growing — conduct a cost audit and eliminate non-core spending.")

    if len(kpi_trends) >= 4 and kpi_trends[3]["direction"] == "UP":
        recommendations.append("Customer growth is strong. Double down on the acquisition channels showing the best CAC.")

    if not recommendations:
        recommendations.append("Business health is good. Set aggressive growth targets for next quarter.")
        recommendations.append("Document what's working and create a repeatable growth playbook.")

    return {
        "kpi_score": round(kpi_score, 1),
        "kpi_trends": kpi_trends,
        "kpi_summary": kpi_summary,
        "explanations": explanations,
        "risk_predictions": risk_predictions,
        "warnings": warnings,
        "recommendations": recommendations,
    }


def _trend_text(metric: str, pct: float, invert: bool = False) -> str:
    good = pct > 0 if not invert else pct < 0
    if abs(pct) <= 2:
        return f"{metric} is stable (±{abs(pct):.1f}%)."
    direction = "increased" if pct > 0 else "decreased"
    sentiment = "✅ positive" if good else "⚠️ concerning"
    return f"{metric} {direction} {abs(pct):.1f}% MoM — {sentiment} signal."


# ─────────────────────────────────────────────────────────────
#  ORIGINAL PROJECT HEALTH ENGINE (preserved)
# ─────────────────────────────────────────────────────────────

def analyze_project(req: ProjectRequest) -> AIAnalysisResponse:
    now = datetime.now(timezone.utc)

    # 1. Basic Stats
    total_tasks = len(req.tasks)
    completed_tasks = sum(1 for t in req.tasks if t.status == "COMPLETED")
    task_comp_rate = (completed_tasks / total_tasks * 100) if total_tasks > 0 else 100.0

    overdue_tasks = 0
    high_priority_overdue = 0
    overdue_task_ids = []
    for t in req.tasks:
        if t.status != "COMPLETED" and t.dueDate and t.dueDate < now:
            overdue_tasks += 1
            if t.priority == "HIGH":
                high_priority_overdue += 1
            overdue_task_ids.append(t.id)

    total_milestones = len(req.milestones)
    completed_milestones = sum(1 for m in req.milestones if m.status == "COMPLETED")
    milestone_progress = (completed_milestones / total_milestones * 100) if total_milestones > 0 else 100.0

    open_risks = sum(1 for r in req.risks if r.status != "CLOSED")
    critical_risks = sum(1 for r in req.risks if r.severity == "CRITICAL" and r.status != "CLOSED")

    # 2. Health Score
    task_score = task_comp_rate * 0.40
    milestone_score = milestone_progress * 0.20
    overdue_penalty = min((overdue_tasks - high_priority_overdue) * 3 + (high_priority_overdue * 8), 25)
    risk_penalty = min((open_risks * 3) + (critical_risks * 7), 20)
    health_score = task_score + milestone_score + 20 - overdue_penalty - risk_penalty

    # ── Blend with Business KPI Score if present ───────────
    biz_result = None
    if req.businessData and req.businessData.monthly_kpis:
        biz_result = analyze_business_kpis(req.businessData)
        kpi_score = biz_result["kpi_score"]
        health_score = health_score * 0.30 + kpi_score * 0.70
        health_score = max(0.0, min(100.0, health_score))
    else:
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
                    "assigned": 0, "completed": 0, "pending": 0, "overdue": 0
                }
            workload[uid]["assigned"] += 1
            if t.status == "COMPLETED":
                workload[uid]["completed"] += 1
            else:
                workload[uid]["pending"] += 1
                if t.dueDate and t.dueDate < now:
                    workload[uid]["overdue"] += 1
                    if t.priority == "HIGH":
                        workload[uid]["high_priority_overdue"] = workload[uid].get("high_priority_overdue", 0) + 1

    # 4. Warnings and Recommendations
    warnings = []
    recommendations = []
    insights = []

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

    # Merge business insights
    if biz_result:
        warnings.extend(biz_result["warnings"])
        recommendations.extend(biz_result["recommendations"])
        insights.extend([f"[KPI] {e['factor']}: {e['reason']}" for e in biz_result["explanations"]])

    if not recommendations:
        recommendations.append("Project is progressing normally.")

    if workload:
        avg_assigned = sum(w["assigned"] for w in workload.values()) / len(workload)
        for uid, w in workload.items():
            if w["assigned"] > avg_assigned * 1.5 and w["assigned"] > 3:
                insights.append(f"[STATISTICAL] Workload imbalance detected: {w['userName']} is handling significantly more tasks than average.")
                recommendations.append(f"Consider reassigning some tasks from {w['userName']} to balance workload.")
            if w.get("high_priority_overdue", 0) >= 2:
                insights.append(f"[RULE_BASED] Burnout risk: {w['userName']} has multiple high-priority overdue tasks.")
                recommendations.append(f"Immediate support needed for {w['userName']} to clear high-priority backlog.")

    # 5. Prediction
    if total_tasks > 0 and completed_tasks > 0:
        if overdue_tasks > completed_tasks:
            prediction = "[STATISTICAL] High likelihood of project delay. Overdue tasks exceed completed tasks."
        elif high_priority_overdue > 0:
            prediction = "[STATISTICAL] Moderate risk of delay due to blocked high-priority tasks."
        else:
            prediction = "[STATISTICAL] Based on current progress, project is on track."
    elif total_tasks == 0:
        prediction = "[RULE_BASED] No tasks assigned yet. Cannot predict completion."
    else:
        prediction = "[RULE_BASED] No tasks completed yet. Risk of significant delay."

    return AIAnalysisResponse(
        projectId=req.projectId,
        projectName=req.projectName,
        healthScore=round(health_score, 1),
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
        insights=insights,
        explanations=biz_result["explanations"] if biz_result else [],
        riskPredictions=biz_result["risk_predictions"] if biz_result else [],
        kpiTrends=biz_result["kpi_trends"] if biz_result else [],
        kpiSummary=biz_result["kpi_summary"] if biz_result else {},
    )
