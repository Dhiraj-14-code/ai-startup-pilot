import React, { useState, useCallback } from 'react';
import { analyzeStartupData, analyzeStartupFile } from '../services/api';

// ─── SVG Line / Area Chart ───────────────────────────────────
const SVGChart = ({ data, keys, colors, height = 200, filled = false }) => {
  if (!data || data.length < 2) return (
    <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
      Add at least 2 months of data to see the chart
    </div>
  );
  const W = 600, H = height;
  const pad = { top: 10, right: 20, bottom: 30, left: 55 };
  const allVals = data.flatMap(d => keys.map(k => d[k] || 0));
  const minV = Math.min(0, ...allVals);
  const maxV = Math.max(...allVals, 1);
  const xStep = (W - pad.left - pad.right) / (data.length - 1);
  const yScale = v => pad.top + (H - pad.top - pad.bottom) * (1 - (v - minV) / (maxV - minV));
  const xScale = i => pad.left + i * xStep;

  const gridLines = 5;
  const yTicks = Array.from({ length: gridLines }, (_, i) => minV + (maxV - minV) * (i / (gridLines - 1)));

  return (
    <div className="chart-area">
      <svg viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none">
        {/* Grid */}
        {yTicks.map((v, i) => (
          <g key={i}>
            <line x1={pad.left} x2={W - pad.right} y1={yScale(v)} y2={yScale(v)}
              stroke="rgba(255,255,255,0.05)" strokeWidth="1" />
            <text x={pad.left - 6} y={yScale(v) + 4} textAnchor="end"
              fill="rgba(255,255,255,0.3)" fontSize="10">
              {v >= 1000 ? `${(v / 1000).toFixed(0)}k` : v.toFixed(0)}
            </text>
          </g>
        ))}
        {/* X labels */}
        {data.map((d, i) => (
          <text key={i} x={xScale(i)} y={H - 4} textAnchor="middle"
            fill="rgba(255,255,255,0.4)" fontSize="9">
            {d.month?.slice(-5) || `M${i + 1}`}
          </text>
        ))}
        {/* Lines / Areas */}
        {keys.map((key, ki) => {
          const pts = data.map((d, i) => `${xScale(i)},${yScale(d[key] || 0)}`).join(' ');
          const zeroY = yScale(0);
          if (filled) {
            const areaPath = `M ${xScale(0)},${zeroY} ` +
              data.map((d, i) => `L ${xScale(i)},${yScale(d[key] || 0)}`).join(' ') +
              ` L ${xScale(data.length - 1)},${zeroY} Z`;
            return (
              <g key={key}>
                <defs>
                  <linearGradient id={`grad-${ki}`} x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor={colors[ki]} stopOpacity="0.3" />
                    <stop offset="100%" stopColor={colors[ki]} stopOpacity="0.02" />
                  </linearGradient>
                </defs>
                <path d={areaPath} fill={`url(#grad-${ki})`} />
                <polyline points={pts} fill="none" stroke={colors[ki]} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
              </g>
            );
          }
          return (
            <polyline key={key} points={pts} fill="none" stroke={colors[ki]}
              strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
          );
        })}
        {/* Data dots */}
        {keys.map((key, ki) =>
          data.map((d, i) => (
            <circle key={`${ki}-${i}`} cx={xScale(i)} cy={yScale(d[key] || 0)} r="3.5"
              fill={colors[ki]} stroke="rgba(0,0,0,0.5)" strokeWidth="1.5" />
          ))
        )}
      </svg>
    </div>
  );
};

// ─── Bar Chart ──────────────────────────────────────────────
const BarChart = ({ data, key: dataKey, color, height = 180 }) => {
  if (!data || data.length === 0) return null;
  const W = 600, H = height;
  const pad = { top: 10, right: 20, bottom: 30, left: 55 };
  const vals = data.map(d => d[dataKey] || 0);
  const maxV = Math.max(...vals.map(Math.abs), 1);
  const barW = Math.max(8, (W - pad.left - pad.right) / data.length - 6);
  const xStep = (W - pad.left - pad.right) / data.length;
  const zeroY = pad.top + (H - pad.top - pad.bottom) / 2;
  const allNeg = vals.every(v => v <= 0);
  const allPos = vals.every(v => v >= 0);
  const yScale = v => allPos ? pad.top + (H - pad.top - pad.bottom) * (1 - v / maxV) :
    allNeg ? pad.top + (H - pad.top - pad.bottom) * (v / maxV + 1) :
    zeroY - ((H - pad.top - pad.bottom) / 2) * (v / maxV);

  return (
    <div className="chart-area">
      <svg viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none">
        <line x1={pad.left} x2={W - pad.right} y1={allPos ? H - pad.bottom : zeroY} y2={allPos ? H - pad.bottom : zeroY}
          stroke="rgba(255,255,255,0.1)" strokeWidth="1" />
        {data.map((d, i) => {
          const v = d[dataKey] || 0;
          const x = pad.left + i * xStep + (xStep - barW) / 2;
          const barColor = v >= 0 ? '#10b981' : '#ef4444';
          const y = allPos ? yScale(v) : v >= 0 ? yScale(v) : zeroY;
          const barH = allPos ? H - pad.bottom - y : Math.abs(yScale(v) - zeroY);
          return (
            <g key={i}>
              <rect x={x} y={y} width={barW} height={Math.max(2, barH)}
                fill={barColor} rx="3" opacity="0.85" />
              <text x={x + barW / 2} y={H - 4} textAnchor="middle"
                fill="rgba(255,255,255,0.35)" fontSize="9">
                {d.month?.slice(-5) || `M${i + 1}`}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
};

// ─── Gauge Ring ─────────────────────────────────────────────
const GaugeRing = ({ score, status }) => {
  const r = 78, cx = 90, cy = 90;
  const circ = 2 * Math.PI * r;
  const dashOffset = circ - (score / 100) * circ;
  const colorMap = { HEALTHY: '#10b981', AT_RISK: '#f59e0b', CRITICAL: '#ef4444', SEVERELY_CRITICAL: '#ef4444' };
  const color = colorMap[status] || '#6366f1';
  return (
    <div className="gauge-ring-wrapper">
      <svg width="180" height="180" viewBox="0 0 180 180">
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="rgba(255,255,255,0.05)" strokeWidth="14" />
        <circle cx={cx} cy={cy} r={r} fill="none" stroke={color} strokeWidth="14"
          strokeDasharray={circ} strokeDashoffset={dashOffset}
          strokeLinecap="round"
          style={{ transition: 'stroke-dashoffset 1s cubic-bezier(0.4,0,0.2,1)', filter: `drop-shadow(0 0 8px ${color})` }} />
      </svg>
      <div className="gauge-center-text">
        <div className="gauge-score">{score?.toFixed(0)}</div>
        <div className="gauge-label">Health</div>
      </div>
    </div>
  );
};

// ─── Default Month Row ───────────────────────────────────────
const emptyMonth = (idx) => ({
  id: Date.now() + idx,
  month: '',
  revenue: '',
  expenses: '',
  customers: '',
  churnRate: '',
});

// ════════════════════════════════════════════════════════════
//  MAIN PAGE
// ════════════════════════════════════════════════════════════
const StartupAnalytics = () => {
  const [tab, setTab] = useState('manual');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [result, setResult] = useState(null);
  const [dragActive, setDragActive] = useState(false);

  // Manual form state
  const [meta, setMeta] = useState({
    funding_raised: '',
    funding_stage: 'Seed',
    industry: 'Technology',
    team_size: '',
  });
  const [months, setMonths] = useState([emptyMonth(0), emptyMonth(1), emptyMonth(2)]);

  // ── Manual Submit ──
  const handleManualSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError(null); setResult(null);
    try {
      const payload = {
        funding_raised: parseFloat(meta.funding_raised) || 0,
        funding_stage: meta.funding_stage,
        industry: meta.industry,
        team_size: parseInt(meta.team_size) || 1,
        monthly_kpis: months
          .filter(m => m.month || m.revenue)
          .map(m => ({
            month: m.month || 'Unknown',
            revenue: parseFloat(m.revenue) || 0,
            expenses: parseFloat(m.expenses) || 0,
            customers: parseInt(m.customers) || 0,
            churnRate: m.churnRate !== '' ? parseFloat(m.churnRate) : null,
          })),
      };
      const res = await analyzeStartupData(payload);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.detail || err.message || 'AI service error. Is it running on port 8000?');
    } finally {
      setLoading(false);
    }
  };

  // ── File Upload ──
  const handleFileDrop = useCallback(async (file) => {
    if (!file) return;
    setLoading(true); setError(null); setResult(null);
    try {
      const res = await analyzeStartupFile(file);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.detail || err.message || 'AI service error. Is it running on port 8000?');
    } finally {
      setLoading(false);
    }
  }, []);

  const handleDrop = (e) => {
    e.preventDefault(); setDragActive(false);
    const file = e.dataTransfer.files[0];
    if (file) handleFileDrop(file);
  };

  // ── Month helpers ──
  const addMonth = () => setMonths(prev => [...prev, emptyMonth(prev.length)]);
  const removeMonth = (id) => setMonths(prev => prev.filter(m => m.id !== id));
  const updateMonth = (id, field, value) =>
    setMonths(prev => prev.map(m => m.id === id ? { ...m, [field]: value } : m));

  // ── Chart data prep ──
  const chartData = result?.kpiSummary ? null : null; // will use result.kpiTrends or manual months
  const manualChartData = months
    .filter(m => m.month || m.revenue)
    .map(m => ({
      month: m.month,
      revenue: parseFloat(m.revenue) || 0,
      expenses: parseFloat(m.expenses) || 0,
      cashFlow: (parseFloat(m.revenue) || 0) - (parseFloat(m.expenses) || 0),
      customers: parseInt(m.customers) || 0,
    }));

  return (
    <div className="analytics-container">
      {/* Header */}
      <div className="analytics-header">
        <h2>🚀 Startup Intelligence Dashboard</h2>
        <p>Enter your business KPIs to get AI-powered health scores, risk predictions, and explainable insights.</p>
      </div>

      {/* Tabs */}
      <div className="analytics-tabs">
        <button className={`tab-btn ${tab === 'manual' ? 'active' : ''}`} onClick={() => setTab('manual')}>
          ✏️ Manual Entry
        </button>
        <button className={`tab-btn ${tab === 'file' ? 'active' : ''}`} onClick={() => setTab('file')}>
          📁 Upload File
        </button>
        {manualChartData.length >= 2 && tab === 'manual' && (
          <button className={`tab-btn ${tab === 'preview' ? 'active' : ''}`} onClick={() => setTab('preview')}>
            📊 Preview Data
          </button>
        )}
      </div>

      {/* ── Manual Entry Tab ── */}
      {tab === 'manual' && (
        <form onSubmit={handleManualSubmit}>
          <div className="card" style={{ marginBottom: '1.5rem' }}>
            <h3 style={{ marginBottom: '1.5rem' }}>Startup Profile</h3>
            <div className="kpi-form-grid">
              <div className="kpi-form-group">
                <label>Funding Raised ($)</label>
                <input type="number" placeholder="e.g. 500000" value={meta.funding_raised}
                  onChange={e => setMeta(p => ({ ...p, funding_raised: e.target.value }))} />
              </div>
              <div className="kpi-form-group">
                <label>Funding Stage</label>
                <select value={meta.funding_stage} onChange={e => setMeta(p => ({ ...p, funding_stage: e.target.value }))}>
                  {['Pre-Seed', 'Seed', 'Series A', 'Series B', 'Series C', 'Bootstrapped', 'Revenue-Stage'].map(s => (
                    <option key={s}>{s}</option>
                  ))}
                </select>
              </div>
              <div className="kpi-form-group">
                <label>Industry</label>
                <select value={meta.industry} onChange={e => setMeta(p => ({ ...p, industry: e.target.value }))}>
                  {['Technology', 'SaaS', 'Fintech', 'Healthtech', 'E-commerce', 'EdTech', 'DeepTech', 'Consumer', 'B2B', 'Other'].map(s => (
                    <option key={s}>{s}</option>
                  ))}
                </select>
              </div>
              <div className="kpi-form-group">
                <label>Team Size</label>
                <input type="number" placeholder="e.g. 8" value={meta.team_size}
                  onChange={e => setMeta(p => ({ ...p, team_size: e.target.value }))} />
              </div>
            </div>
          </div>

          <div className="card" style={{ marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h3 style={{ margin: 0 }}>Monthly KPIs</h3>
              <button type="button" className="btn-add-month" onClick={addMonth}>+ Add Month</button>
            </div>

            {/* Column headers */}
            <div style={{ display: 'grid', gridTemplateColumns: '120px repeat(5, 1fr) auto', gap: '0.75rem', marginBottom: '0.5rem', padding: '0 1rem' }}>
              {['Month', 'Revenue ($)', 'Expenses ($)', 'Customers', 'Churn (%)', 'Notes', ''].map((h, i) => (
                <div key={i} className="kpi-col-label">{h}</div>
              ))}
            </div>

            <div className="kpi-months-list">
              {months.map(m => (
                <div key={m.id} className="kpi-month-row">
                  <input placeholder="2025-01" value={m.month}
                    onChange={e => updateMonth(m.id, 'month', e.target.value)} />
                  <input type="number" placeholder="0" value={m.revenue}
                    onChange={e => updateMonth(m.id, 'revenue', e.target.value)} />
                  <input type="number" placeholder="0" value={m.expenses}
                    onChange={e => updateMonth(m.id, 'expenses', e.target.value)} />
                  <input type="number" placeholder="0" value={m.customers}
                    onChange={e => updateMonth(m.id, 'customers', e.target.value)} />
                  <input type="number" placeholder="0" step="0.1" value={m.churnRate}
                    onChange={e => updateMonth(m.id, 'churnRate', e.target.value)} />
                  <div />
                  <button type="button" className="btn-remove-month" onClick={() => removeMonth(m.id)}>✕</button>
                </div>
              ))}
            </div>

            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
              <button type="submit" className="btn-analyze" disabled={loading}>
                {loading ? '⏳ Analyzing...' : '🔍 Analyze Business'}
              </button>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                {months.filter(m => m.month || m.revenue).length} month(s) of data
              </span>
            </div>
          </div>
        </form>
      )}

      {/* ── File Upload Tab ── */}
      {tab === 'file' && (
        <div className="card">
          <h3 style={{ marginBottom: '0.5rem' }}>Upload Business Data</h3>
          <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
            Upload a CSV or XLSX with columns: <code style={{ background: 'rgba(255,255,255,0.08)', padding: '0.1rem 0.4rem', borderRadius: '4px' }}>month, revenue, expenses, customers, churn_rate</code>
          </p>

          <div
            className={`upload-zone ${dragActive ? 'drag-active' : ''}`}
            onDragOver={e => { e.preventDefault(); setDragActive(true); }}
            onDragLeave={() => setDragActive(false)}
            onDrop={handleDrop}
          >
            <div className="upload-icon">📊</div>
            <div className="upload-text">Drag & drop your CSV / XLSX / JSON here</div>
            <div className="upload-hint">Or click to browse — auto-detects business KPI columns</div>
            <input type="file" accept=".csv,.xlsx,.json,.pdf"
              onChange={e => handleFileDrop(e.target.files[0])} />
          </div>

          {loading && (
            <div style={{ textAlign: 'center', marginTop: '2rem', color: 'var(--primary)' }}>
              ⏳ Analyzing your business data...
            </div>
          )}
        </div>
      )}

      {/* ── Preview Chart Tab ── */}
      {tab === 'preview' && manualChartData.length >= 2 && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
          <div className="chart-card">
            <h4>Revenue vs Expenses</h4>
            <SVGChart data={manualChartData} keys={['revenue', 'expenses']} colors={['#6366f1', '#ef4444']} height={200} filled />
            <div className="chart-legend">
              <div className="legend-item"><div className="legend-dot" style={{ background: '#6366f1' }} /> Revenue</div>
              <div className="legend-item"><div className="legend-dot" style={{ background: '#ef4444' }} /> Expenses</div>
            </div>
          </div>
          <div className="chart-card">
            <h4>Net Cash Flow</h4>
            <BarChart data={manualChartData} key="cashFlow" color="#10b981" height={200} />
          </div>
        </div>
      )}

      {/* ── Error ── */}
      {error && (
        <div className="upload-error" style={{ marginTop: '1.5rem' }}>
          <strong>⚠ Analysis Failed:</strong> {error}
        </div>
      )}

      {/* ══════════════════════════════════════════════
          RESULTS
      ══════════════════════════════════════════════ */}
      {result && (
        <div className="results-layout">
          {/* LEFT: Health Gauge + KPI Summary */}
          <div>
            <div className="health-gauge-card">
              <GaugeRing score={result.healthScore} status={result.healthStatus} />
              <div className={`gauge-status ${result.healthStatus?.toLowerCase()}`}>
                {result.healthStatus?.replace('_', ' ')}
              </div>

              {result.kpiSummary && Object.keys(result.kpiSummary).length > 0 && (
                <div className="kpi-summary-grid">
                  {result.kpiSummary.runway_months != null && (
                    <div className="kpi-stat-row">
                      <span className="kpi-stat-label">💰 Runway</span>
                      <span className={`kpi-stat-value ${result.kpiSummary.runway_months < 4 ? 'negative' : result.kpiSummary.runway_months < 9 ? 'warning' : 'positive'}`}>
                        {result.kpiSummary.runway_months} mo
                      </span>
                    </div>
                  )}
                  {result.kpiSummary.latest_revenue != null && (
                    <div className="kpi-stat-row">
                      <span className="kpi-stat-label">📈 Latest MRR</span>
                      <span className="kpi-stat-value">${result.kpiSummary.latest_revenue?.toLocaleString()}</span>
                    </div>
                  )}
                  {result.kpiSummary.avg_monthly_burn != null && (
                    <div className="kpi-stat-row">
                      <span className="kpi-stat-label">🔥 Avg Burn</span>
                      <span className={`kpi-stat-value ${result.kpiSummary.avg_monthly_burn > result.kpiSummary.latest_revenue ? 'negative' : 'positive'}`}>
                        ${result.kpiSummary.avg_monthly_burn?.toLocaleString()}
                      </span>
                    </div>
                  )}
                  {result.kpiSummary.latest_customers != null && (
                    <div className="kpi-stat-row">
                      <span className="kpi-stat-label">👥 Customers</span>
                      <span className="kpi-stat-value">{result.kpiSummary.latest_customers?.toLocaleString()}</span>
                    </div>
                  )}
                  {result.kpiSummary.latest_churn_rate != null && (
                    <div className="kpi-stat-row">
                      <span className="kpi-stat-label">↩ Churn</span>
                      <span className={`kpi-stat-value ${result.kpiSummary.latest_churn_rate > 5 ? 'negative' : 'positive'}`}>
                        {result.kpiSummary.latest_churn_rate}%
                      </span>
                    </div>
                  )}
                  {result.kpiSummary.funding_stage && (
                    <div className="kpi-stat-row">
                      <span className="kpi-stat-label">🎯 Stage</span>
                      <span className="kpi-stat-value">{result.kpiSummary.funding_stage}</span>
                    </div>
                  )}
                  {result.kpiSummary.team_size != null && (
                    <div className="kpi-stat-row">
                      <span className="kpi-stat-label">🏢 Team</span>
                      <span className="kpi-stat-value">{result.kpiSummary.team_size} people</span>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* RIGHT: Charts + Insights */}
          <div className="results-right">
            {/* AI Prediction Banner */}
            {result.prediction && (
              <div className="prediction-banner">{result.prediction}</div>
            )}

            {/* KPI Trend Badges */}
            {result.kpiTrends && result.kpiTrends.length > 0 && (
              <div className="chart-card">
                <h4>KPI Trends (MoM)</h4>
                <div className="kpi-trends-grid">
                  {result.kpiTrends.map((t, i) => {
                    const dir = t.direction?.toLowerCase();
                    const icon = dir === 'up' ? '↑' : dir === 'down' ? '↓' : '→';
                    return (
                      <div key={i} className="trend-badge">
                        <div className="trend-metric">{t.metric}</div>
                        <div className={`trend-value ${dir}`}>
                          {icon} {Math.abs(t.change_pct)?.toFixed(1)}%
                        </div>
                        <div className="trend-interpret">{t.interpretation}</div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Revenue Chart (from manual data if available) */}
            {manualChartData.length >= 2 && (
              <div className="chart-card">
                <h4>Revenue vs Expenses</h4>
                <SVGChart data={manualChartData} keys={['revenue', 'expenses']}
                  colors={['#6366f1', '#ef4444']} height={200} filled />
                <div className="chart-legend">
                  <div className="legend-item"><div className="legend-dot" style={{ background: '#6366f1' }} /> Revenue</div>
                  <div className="legend-item"><div className="legend-dot" style={{ background: '#ef4444' }} /> Expenses</div>
                </div>
              </div>
            )}

            {manualChartData.length >= 2 && (
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                <div className="chart-card">
                  <h4>Net Cash Flow</h4>
                  <BarChart data={manualChartData} key="cashFlow" color="#10b981" height={180} />
                </div>
                <div className="chart-card">
                  <h4>Customer Growth</h4>
                  <SVGChart data={manualChartData} keys={['customers']} colors={['#8b5cf6']} height={180} />
                </div>
              </div>
            )}

            {/* Explainability Panel */}
            {result.explanations && result.explanations.length > 0 && (
              <div className="chart-card">
                <h4>🧠 Why This Score? (Explainable AI)</h4>
                <div className="explain-list">
                  {result.explanations.map((e, i) => {
                    const impact = e.impact?.toLowerCase();
                    const barPct = Math.min(Math.abs(e.score_impact || 0) / 25 * 100, 100);
                    return (
                      <div key={i} className="explain-item">
                        <div className="explain-header">
                          <span className="explain-factor">{e.factor}</span>
                          <span className={`explain-impact-badge ${impact}`}>
                            {e.score_impact > 0 ? '+' : ''}{e.score_impact} pts
                          </span>
                        </div>
                        <div className="explain-bar-track">
                          <div className={`explain-bar-fill ${impact}`}
                            style={{ width: `${barPct}%` }} />
                        </div>
                        <div className="explain-reason">{e.reason}</div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Risk Predictions */}
            {result.riskPredictions && result.riskPredictions.length > 0 && (
              <div className="chart-card">
                <h4>⚠️ Risk Predictions</h4>
                <div className="risk-cards-grid">
                  {result.riskPredictions.map((r, i) => {
                    const prob = r.probability?.toLowerCase();
                    return (
                      <div key={i} className={`risk-card ${prob}`}>
                        <div className="risk-card-header">
                          <div className="risk-name">{r.risk}</div>
                          <div className={`risk-prob ${prob}`}>{r.probability}</div>
                        </div>
                        <div className="risk-timeframe">{r.timeframe}</div>
                        <div className="risk-explain">{r.explanation}</div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Recommendations */}
            {result.recommendations && result.recommendations.length > 0 && (
              <div className="chart-card">
                <h4>✅ Actionable Recommendations</h4>
                <div className="rec-list">
                  {result.recommendations.map((r, i) => (
                    <div key={i} className="rec-item">{r}</div>
                  ))}
                </div>
              </div>
            )}

            {/* Warnings */}
            {result.warnings && result.warnings.length > 0 && (
              <div className="chart-card" style={{ borderColor: 'rgba(245,158,11,0.2)' }}>
                <h4>⚡ Warnings</h4>
                <div className="rec-list">
                  {result.warnings.map((w, i) => (
                    <div key={i} className="rec-item" style={{ borderColor: 'rgba(245,158,11,0.15)', color: '#fbbf24' }}>{w}</div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default StartupAnalytics;
