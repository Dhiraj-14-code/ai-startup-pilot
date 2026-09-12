import React from 'react';

// ── Reusable sub-components ───────────────────────────────────
const Section = ({ title, colorClass, children }) => (
  <div className={`section ${colorClass || ''}`}>
    <h4>{title}</h4>
    {children}
  </div>
);

const ListItems = ({ items }) => (
  <ul>
    {items.map((item, idx) => <li key={idx}>{item}</li>)}
  </ul>
);

// ── KPI Trend Badges ─────────────────────────────────────────
const KPITrends = ({ trends }) => {
  if (!trends || trends.length === 0) return null;
  return (
    <div className="section">
      <h4>📊 KPI Trends</h4>
      <div className="kpi-trends-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '0.75rem' }}>
        {trends.map((t, i) => {
          const dir = t.direction?.toLowerCase();
          const icon = dir === 'up' ? '↑' : dir === 'down' ? '↓' : '→';
          const color = dir === 'up' ? '#10b981' : dir === 'down' ? '#ef4444' : '#f59e0b';
          return (
            <div key={i} style={{
              background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.07)',
              borderRadius: '10px', padding: '0.875rem',
            }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.3rem' }}>{t.metric}</div>
              <div style={{ fontSize: '1.3rem', fontWeight: 700, color, fontFamily: 'Outfit, sans-serif' }}>
                {icon} {Math.abs(t.change_pct ?? 0).toFixed(1)}%
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem', lineHeight: 1.4 }}>{t.interpretation}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

// ── Explainability Panel ──────────────────────────────────────
const Explanations = ({ explanations }) => {
  if (!explanations || explanations.length === 0) return null;
  return (
    <div className="section">
      <h4>🧠 Explainable AI — Score Factors</h4>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        {explanations.map((e, i) => {
          const impact = e.impact?.toLowerCase();
          const barPct = Math.min(Math.abs(e.score_impact || 0) / 25 * 100, 100);
          const barColor = impact === 'positive' ? '#10b981' : impact === 'negative' ? '#ef4444' : '#f59e0b';
          return (
            <div key={i} style={{
              background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.06)',
              borderRadius: '10px', padding: '0.875rem',
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ fontWeight: 600, fontSize: '0.9rem', color: '#fff' }}>{e.factor}</span>
                <span style={{
                  fontSize: '0.72rem', fontWeight: 700, padding: '0.2rem 0.65rem',
                  borderRadius: '999px', background: `${barColor}22`, color: barColor,
                  border: `1px solid ${barColor}44`,
                }}>
                  {e.score_impact > 0 ? '+' : ''}{e.score_impact} pts
                </span>
              </div>
              <div style={{ height: '5px', borderRadius: '999px', background: 'rgba(255,255,255,0.07)', overflow: 'hidden', marginBottom: '0.5rem' }}>
                <div style={{ height: '100%', width: `${barPct}%`, background: barColor, borderRadius: '999px', transition: 'width 0.8s ease' }} />
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: 1.5 }}>{e.reason}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

// ── Risk Prediction Cards ─────────────────────────────────────
const RiskPredictions = ({ risks }) => {
  if (!risks || risks.length === 0) return null;
  return (
    <div className="section">
      <h4>⚠️ Risk Predictions</h4>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '1rem' }}>
        {risks.map((r, i) => {
          const prob = r.probability?.toLowerCase();
          const colors = { high: '#ef4444', medium: '#f59e0b', low: '#10b981' };
          const c = colors[prob] || '#6366f1';
          return (
            <div key={i} style={{
              borderRadius: '14px', padding: '1.25rem',
              background: `${c}11`, border: `1px solid ${c}33`,
              position: 'relative', overflow: 'hidden',
            }}>
              <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '3px', background: c }} />
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem', gap: '0.5rem' }}>
                <strong style={{ fontSize: '0.9rem', color: '#fff' }}>{r.risk}</strong>
                <span style={{
                  fontSize: '0.68rem', fontWeight: 800, padding: '0.2rem 0.6rem',
                  borderRadius: '999px', background: `${c}22`, color: c,
                  textTransform: 'uppercase', letterSpacing: '0.1em', flexShrink: 0,
                }}>{r.probability}</span>
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>🕐 {r.timeframe}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: 1.55 }}>{r.explanation}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

// ── Main Component ────────────────────────────────────────────
const AIInsights = ({ health }) => {
  return (
    <div className="card ai-insights">
      <h3>AI Health Analysis</h3>

      <div className="score-section">
        <div className={`score-circle ${health.healthStatus?.toLowerCase()}`}>
          {health.healthScore?.toFixed(0)}
        </div>
        <div className="status-text">{health.healthStatus?.replace(/_/g, ' ')}</div>
      </div>

      <div className="metrics-grid">
        <div className="metric-card">
          <div className="metric-value">{health.overdueTasks || 0}</div>
          <div className="metric-label">Overdue Tasks</div>
        </div>
        <div className="metric-card">
          <div className="metric-value">{health.taskCompletionRate?.toFixed(0)}%</div>
          <div className="metric-label">Task Completion</div>
        </div>
        <div className="metric-card">
          <div className="metric-value">{health.criticalRisks || 0}</div>
          <div className="metric-label">Critical Risks</div>
        </div>
        <div className="metric-card">
          <div className="metric-value">{health.milestoneProgress?.toFixed(0)}%</div>
          <div className="metric-label">Milestone Progress</div>
        </div>
      </div>

      <div className="insights-grid">
        {health.prediction && (
          <Section title="🤖 Prediction">
            <p style={{ color: 'var(--text-muted)', lineHeight: 1.6, margin: 0 }}>{health.prediction}</p>
          </Section>
        )}

        {/* NEW: KPI Trends */}
        {health.kpiTrends && health.kpiTrends.length > 0 && (
          <KPITrends trends={health.kpiTrends} />
        )}

        {/* NEW: Explainability */}
        {health.explanations && health.explanations.length > 0 && (
          <Explanations explanations={health.explanations} />
        )}

        {/* NEW: Risk Predictions */}
        {health.riskPredictions && health.riskPredictions.length > 0 && (
          <RiskPredictions risks={health.riskPredictions} />
        )}

        {health.insights && health.insights.length > 0 && (
          <Section title="Insights">
            <ListItems items={health.insights} />
          </Section>
        )}

        {health.warnings && health.warnings.length > 0 && (
          <Section title="⚡ Warnings" colorClass="warnings">
            <ListItems items={health.warnings} />
          </Section>
        )}

        {health.recommendations && health.recommendations.length > 0 && (
          <Section title="✅ Recommendations" colorClass="recommendations">
            <ListItems items={health.recommendations} />
          </Section>
        )}

        {health.workloadAnalysis && Object.keys(health.workloadAnalysis).length > 0 && (
          <Section title="👥 Workload Analysis" colorClass="workload">
            {Object.values(health.workloadAnalysis).map((w, idx) => (
              <div key={idx} className="workload-item">
                <span className="workload-user">{w.userName}</span>
                <span className="workload-stats">
                  <span>Assigned: {w.assigned}</span>
                  <span>Completed: {w.completed}</span>
                  {w.overdue > 0 && <span className="error">Overdue: {w.overdue}</span>}
                </span>
              </div>
            ))}
          </Section>
        )}
      </div>
    </div>
  );
};

export default AIInsights;
