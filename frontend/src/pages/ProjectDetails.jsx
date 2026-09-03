import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getProject, getProjectHealth } from '../api';
import AIInsights from '../components/AIInsights';

const ProjectDetails = () => {
  const { id } = useParams();
  const [project, setProject] = useState(null);
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [projRes, healthRes] = await Promise.all([
          getProject(id),
          getProjectHealth(id)
        ]);
        setProject(projRes.data);
        setHealth(healthRes.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [id]);

  if (loading) return <div>Loading...</div>;
  if (!project) return <div>Project not found</div>;

  return (
    <div className="project-details-container">
      <div className="header-actions">
        <Link to="/" className="back-link">← Back to Dashboard</Link>
      </div>
      
      <h2>{project.name}</h2>
      <p>{project.description}</p>
      
      <div className="layout-grid">
        <div className="main-col">
          <div className="card">
            <h3>Tasks</h3>
            <ul>
              {project.tasks?.map(t => (
                <li key={t.id}>
                  <strong>{t.title}</strong> - {t.status} ({t.priority})
                </li>
              ))}
              {(!project.tasks || project.tasks.length === 0) && <li>No tasks</li>}
            </ul>
          </div>
          
          <div className="card">
            <h3>Risks</h3>
            <ul>
              {project.risks?.map(r => (
                <li key={r.id}>
                  <strong>{r.title}</strong> - {r.severity} - {r.status}
                </li>
              ))}
              {(!project.risks || project.risks.length === 0) && <li>No risks</li>}
            </ul>
          </div>
          
          <div className="card">
            <h3>Milestones</h3>
            <ul>
              {project.milestones?.map(m => (
                <li key={m.id}>
                  <strong>{m.title}</strong> - {m.status}
                </li>
              ))}
              {(!project.milestones || project.milestones.length === 0) && <li>No milestones</li>}
            </ul>
          </div>
        </div>
        
        <div className="side-col">
          {health && <AIInsights health={health} />}
        </div>
      </div>
    </div>
  );
};

export default ProjectDetails;
