import React from 'react';

const AIInsights = ({ health }) => {
  return (
    <div className="card ai-insights">
      <h3>AI Health Analysis</h3>
      
      <div className="score-section">
        <div className={`score-circle ${health.healthStatus?.toLowerCase()}`}>
          {health.healthScore?.toFixed(0)}
        </div>
        <p className="status-text">{health.healthStatus}</p>
      </div>
      
      <div className="metrics">
        <p><strong>Overdue Tasks:</strong> {health.overdueTasks}</p>
        <p><strong>Task Completion:</strong> {health.taskCompletionRate?.toFixed(1)}%</p>
        <p><strong>Critical Risks:</strong> {health.criticalRisks}</p>
      </div>

      {health.prediction && (
        <div className="section prediction">
          <h4>Prediction</h4>
          <p>{health.prediction}</p>
        </div>
      )}

      {health.insights && health.insights.length > 0 && (
        <div className="section insights">
          <h4>Insights</h4>
          <ul>
            {health.insights.map((insight, idx) => (
              <li key={idx}>{insight}</li>
            ))}
          </ul>
        </div>
      )}

      {health.warnings && health.warnings.length > 0 && (
        <div className="section warnings">
          <h4>Warnings</h4>
          <ul>
            {health.warnings.map((w, idx) => (
              <li key={idx}>{w}</li>
            ))}
          </ul>
        </div>
      )}

      {health.recommendations && health.recommendations.length > 0 && (
        <div className="section recommendations">
          <h4>Recommendations</h4>
          <ul>
            {health.recommendations.map((r, idx) => (
              <li key={idx}>{r}</li>
            ))}
          </ul>
        </div>
      )}
      
      {health.workloadAnalysis && Object.keys(health.workloadAnalysis).length > 0 && (
        <div className="section workload">
          <h4>Workload Analysis</h4>
          {Object.values(health.workloadAnalysis).map((w, idx) => (
            <div key={idx} className="workload-item">
              <span>{w.userName}</span>
              <span>Assigned: {w.assigned}, Completed: {w.completed}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AIInsights;
