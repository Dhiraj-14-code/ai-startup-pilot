import React, { useEffect, useState } from 'react';
import { getProjects, uploadFileForAnalysis } from '../api';
import { Link } from 'react-router-dom';

const Dashboard = () => {
  const [projects, setProjects] = useState([]);
  const [file, setFile] = useState(null);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchProjects();
  }, []);

  const fetchProjects = async () => {
    try {
      const res = await getProjects();
      setProjects(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleFileUpload = async (e) => {
    e.preventDefault();
    if (!file) return;
    setLoading(true);
    try {
      const res = await uploadFileForAnalysis(file);
      setAnalysisResult(res.data);
    } catch (err) {
      console.error(err);
      alert('Error uploading file');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-container">
      <h2>Your Projects</h2>
      <div className="projects-grid">
        {projects.map(p => (
          <Link key={p.id} to={`/projects/${p.id}`} className="project-card">
            <h3>{p.name}</h3>
            <p>{p.description}</p>
            <span className="status-badge">{p.status}</span>
          </Link>
        ))}
      </div>

      <div className="file-upload-section">
        <h3>AI File Import & Analysis</h3>
        <form onSubmit={handleFileUpload}>
          <input type="file" onChange={e => setFile(e.target.files[0])} accept=".json,.csv,.xlsx,.pdf" />
          <button type="submit" disabled={loading}>
            {loading ? 'Analyzing...' : 'Upload & Analyze'}
          </button>
        </form>

        {analysisResult && (
          <div className="analysis-result card">
            <h4>Import Analysis Result</h4>
            <p><strong>Health Score:</strong> {analysisResult.healthScore}</p>
            <p><strong>Status:</strong> {analysisResult.healthStatus}</p>
            <p><strong>Prediction:</strong> {analysisResult.prediction}</p>
            <div>
              <strong>Recommendations:</strong>
              <ul>
                {analysisResult.recommendations?.map((r, i) => <li key={i}>{r}</li>)}
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
